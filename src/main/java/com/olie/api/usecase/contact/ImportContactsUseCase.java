package com.olie.api.usecase.contact;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.contact.ContactFileError;
import com.olie.api.dto.contact.ContactFileFormat;
import com.olie.api.dto.contact.ContactImportResponse;
import com.olie.api.entity.Contact;
import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.repository.ContactCategoryRepository;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;

/**
 * Importa contatos e categorias de um arquivo YAML/JSON:
 * <pre>
 * categories: [Farmácias, Fornecedores]
 * contacts:
 *   - name: Farmácia Central
 *     phone: "+5511999999999"
 *     categories: [Farmácias]
 *     active: true          # opcional, padrão true
 *     doNotContact: false   # opcional, padrão false
 * </pre>
 * Tudo ou nada: com qualquer erro nada é gravado e a resposta lista cada problema com a linha. Categorias são
 * casadas pelo nome e contatos pelo telefone (existentes são atualizados); o que não está no arquivo é mantido.
 */
@Service
@RequiredArgsConstructor
public class ImportContactsUseCase {

    private record Entry<T>(T value, int line) {
    }

    private record ParsedContact(
            String name, String phone, List<String> categories, boolean active, boolean doNotContact, int line) {
    }

    private final ContactRepository contactRepository;
    private final ContactCategoryRepository contactCategoryRepository;

    @Transactional
    public ContactImportResponse execute(User user, byte[] content, ContactFileFormat format) {
        List<ContactFileError> errors = new ArrayList<>();
        List<Entry<String>> declaredCategories = new ArrayList<>();
        List<Entry<JsonNode>> rawContacts = new ArrayList<>();

        parse(content, format, declaredCategories, rawContacts, errors);

        Map<String, ContactCategory> existingCategories = new HashMap<>();
        contactCategoryRepository.findAllByUserIdOrderByNameAsc(user.getId())
                .forEach(category -> existingCategories.put(key(category.getName()), category));

        Map<String, String> knownCategoryNames = new LinkedHashMap<>();
        existingCategories.values().forEach(category -> knownCategoryNames.put(key(category.getName()), category.getName()));
        declaredCategories.forEach(entry -> knownCategoryNames.putIfAbsent(key(entry.value()), entry.value()));

        List<ParsedContact> contacts = validateContacts(rawContacts, knownCategoryNames, errors);

        if (!errors.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CONTACTS_FILE",
                    "O arquivo tem %d problema(s); nada foi importado.".formatted(errors.size()), errors);
        }

        return apply(user, declaredCategories, contacts, existingCategories);
    }

    private void parse(byte[] content, ContactFileFormat format, List<Entry<String>> categories,
            List<Entry<JsonNode>> contacts, List<ContactFileError> errors) {
        try (JsonParser parser = format.mapper().createParser(content)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                errors.add(new ContactFileError(line(parser), null,
                        "O arquivo deve ter as seções \"categories\" e \"contacts\"."));
                return;
            }

            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String section = parser.currentName();
                JsonToken token = parser.nextToken();

                if (!"categories".equals(section) && !"contacts".equals(section)) {
                    errors.add(new ContactFileError(line(parser), section, "Seção desconhecida."));
                    parser.skipChildren();
                    continue;
                }
                if (token != JsonToken.START_ARRAY) {
                    errors.add(new ContactFileError(line(parser), section, "Deve ser uma lista."));
                    parser.skipChildren();
                    continue;
                }

                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    int line = line(parser);
                    JsonNode node = parser.readValueAsTree();
                    if ("categories".equals(section)) {
                        if (node.isString() && !node.asString().isBlank()) {
                            categories.add(new Entry<>(node.asString().trim(), line));
                        } else {
                            errors.add(new ContactFileError(line, "categories", "Nome de categoria inválido."));
                        }
                    } else {
                        contacts.add(new Entry<>(node, line));
                    }
                }
            }
        } catch (JacksonException exception) {
            int line = exception.getLocation() != null ? exception.getLocation().getLineNr() : 0;
            errors.add(new ContactFileError(line, null, "Arquivo mal formatado: " + exception.getOriginalMessage()));
        }
    }

    private List<ParsedContact> validateContacts(List<Entry<JsonNode>> rawContacts,
            Map<String, String> knownCategoryNames, List<ContactFileError> errors) {
        List<ParsedContact> contacts = new ArrayList<>();
        Map<String, Integer> phoneLines = new HashMap<>();

        for (Entry<JsonNode> entry : rawContacts) {
            JsonNode node = entry.value();
            int line = entry.line();
            int errorsBefore = errors.size();

            if (!node.isObject()) {
                errors.add(new ContactFileError(line, null, "Cada contato deve ser um objeto com nome e telefone."));
                continue;
            }

            String name = text(node, "name");
            if (name == null) {
                errors.add(new ContactFileError(line, "name", "Nome é obrigatório."));
            }

            Optional<String> phone = PhoneNumbers.normalize(text(node, "phone"));
            if (phone.isEmpty()) {
                errors.add(new ContactFileError(line, "phone",
                        "Telefone inválido: use o formato internacional, ex.: +5511999999999."));
            } else {
                Integer firstLine = phoneLines.putIfAbsent(phone.get(), line);
                if (firstLine != null) {
                    errors.add(new ContactFileError(line, "phone",
                            "Telefone %s repetido (já usado na linha %d).".formatted(phone.get(), firstLine)));
                }
            }

            List<String> categories = new ArrayList<>();
            JsonNode categoriesNode = node.get("categories");
            if (categoriesNode == null || !categoriesNode.isArray() || categoriesNode.isEmpty()) {
                errors.add(new ContactFileError(line, "categories", "Informe ao menos uma categoria."));
            } else {
                for (JsonNode category : categoriesNode) {
                    String canonical = category.isString() ? knownCategoryNames.get(key(category.asString())) : null;
                    if (canonical == null) {
                        errors.add(new ContactFileError(line, "categories",
                                "Categoria \"%s\" não existe; declare-a em \"categories\".".formatted(category.asString())));
                    } else {
                        categories.add(canonical);
                    }
                }
            }

            Boolean active = bool(node, "active", true, line, errors);
            Boolean doNotContact = bool(node, "doNotContact", false, line, errors);

            if (errors.size() == errorsBefore) {
                contacts.add(new ParsedContact(name, phone.get(), categories, active, doNotContact, line));
            }
        }

        return contacts;
    }

    private ContactImportResponse apply(User user, List<Entry<String>> declaredCategories,
            List<ParsedContact> contacts, Map<String, ContactCategory> categoriesByKey) {
        int categoriesCreated = 0;
        for (Entry<String> declared : declaredCategories) {
            if (!categoriesByKey.containsKey(key(declared.value()))) {
                ContactCategory category = ContactCategory.builder().user(user).name(declared.value()).build();
                contactCategoryRepository.save(category);
                categoriesByKey.put(key(declared.value()), category);
                categoriesCreated++;
            }
        }

        int created = 0;
        int updated = 0;
        for (ParsedContact parsed : contacts) {
            Set<ContactCategory> categories = new LinkedHashSet<>();
            parsed.categories().forEach(name -> categories.add(categoriesByKey.get(key(name))));

            Optional<Contact> existing = contactRepository.findByUserIdAndPhone(user.getId(), parsed.phone());
            Contact contact = existing.orElseGet(() -> Contact.builder().user(user).phone(parsed.phone()).build());
            contact.setName(parsed.name());
            contact.setActive(parsed.active());
            contact.setDoNotContact(parsed.doNotContact());
            contact.setCategories(categories);
            contact.setUpdatedAt(Instant.now());
            contactRepository.save(contact);

            if (existing.isPresent()) {
                updated++;
            } else {
                created++;
            }
        }

        return new ContactImportResponse(categoriesCreated, created, updated);
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static int line(JsonParser parser) {
        return parser.currentTokenLocation().getLineNr();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !value.isValueNode()) {
            return null;
        }
        String text = value.asString().trim();
        return text.isEmpty() ? null : text;
    }

    private static Boolean bool(JsonNode node, String field, boolean defaultValue, int line,
            List<ContactFileError> errors) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        if (!value.isBoolean()) {
            errors.add(new ContactFileError(line, field, "Deve ser true ou false."));
            return defaultValue;
        }
        return value.asBoolean();
    }
}

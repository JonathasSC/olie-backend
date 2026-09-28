package com.olie.api.dto.contact;

import java.util.Locale;
import java.util.Optional;

import org.springframework.http.MediaType;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

public enum ContactFileFormat {
    YAML("yaml", MediaType.parseMediaType("application/yaml"), YAMLMapper.builder().build()),
    JSON("json", MediaType.APPLICATION_JSON, JsonMapper.builder().build());

    private final String extension;
    private final MediaType mediaType;
    private final ObjectMapper mapper;

    ContactFileFormat(String extension, MediaType mediaType, ObjectMapper mapper) {
        this.extension = extension;
        this.mediaType = mediaType;
        this.mapper = mapper;
    }

    /** O formato é decidido pela extensão do arquivo: .yaml/.yml ou .json. */
    public static Optional<ContactFileFormat> fromFilename(String filename) {
        if (filename == null) {
            return Optional.empty();
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) {
            return Optional.of(YAML);
        }
        if (lower.endsWith(".json")) {
            return Optional.of(JSON);
        }
        return Optional.empty();
    }

    public String extension() {
        return extension;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public ObjectMapper mapper() {
        return mapper;
    }
}

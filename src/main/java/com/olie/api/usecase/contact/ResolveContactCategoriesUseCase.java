package com.olie.api.usecase.contact;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveContactCategoriesUseCase {

    private final ContactCategoryRepository contactCategoryRepository;

    public Set<ContactCategory> execute(User user, Set<UUID> categoryIds) {
        List<ContactCategory> categories = contactCategoryRepository.findAllByIdInAndUserId(categoryIds, user.getId());
        if (categories.size() != categoryIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact category not found");
        }
        return new LinkedHashSet<>(categories);
    }
}

package com.olie.api.usecase.planneditem;

import org.springframework.stereotype.Service;

import com.olie.api.dto.planneditem.PlannedItemRequest;
import com.olie.api.dto.planneditem.PlannedItemResponse;
import com.olie.api.entity.Category;
import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.User;
import com.olie.api.repository.PlannedItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePlannedItemUseCase {

    private final PlannedItemRepository plannedItemRepository;
    private final ResolvePlannedItemCategoryUseCase resolvePlannedItemCategoryUseCase;

    public PlannedItemResponse execute(User user, PlannedItemRequest request) {
        Category category = resolvePlannedItemCategoryUseCase.execute(user, request.categoryId());

        PlannedItem plannedItem = PlannedItem.builder()
                .user(user)
                .name(request.name())
                .priority(request.priority())
                .estimatedValue(request.estimatedValue())
                .estimatedDate(request.estimatedDate())
                .category(category)
                .build();

        plannedItemRepository.save(plannedItem);

        return PlannedItemResponse.from(plannedItem);
    }
}

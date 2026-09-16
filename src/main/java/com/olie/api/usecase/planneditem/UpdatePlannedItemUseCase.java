package com.olie.api.usecase.planneditem;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.planneditem.PlannedItemRequest;
import com.olie.api.dto.planneditem.PlannedItemResponse;
import com.olie.api.entity.Category;
import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.User;
import com.olie.api.repository.PlannedItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePlannedItemUseCase {

    private final PlannedItemRepository plannedItemRepository;
    private final ResolvePlannedItemCategoryUseCase resolvePlannedItemCategoryUseCase;

    public PlannedItemResponse execute(User user, UUID plannedItemId, PlannedItemRequest request) {
        PlannedItem plannedItem = plannedItemRepository.findByIdAndUserId(plannedItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Planned item not found"));

        Category category = resolvePlannedItemCategoryUseCase.execute(user, request.categoryId());

        plannedItem.setName(request.name());
        plannedItem.setPriority(request.priority());
        plannedItem.setEstimatedValue(request.estimatedValue());
        plannedItem.setEstimatedDate(request.estimatedDate());
        plannedItem.setCategory(category);
        plannedItem.setUpdatedAt(Instant.now());

        // dados usados pelas condições de notificação mudaram: permite alertar novamente
        plannedItem.setDateAlertSent(false);
        plannedItem.setBalanceAlertSent(false);

        plannedItemRepository.save(plannedItem);

        return PlannedItemResponse.from(plannedItem);
    }
}

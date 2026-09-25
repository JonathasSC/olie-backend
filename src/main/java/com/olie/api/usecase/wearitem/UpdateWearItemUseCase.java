package com.olie.api.usecase.wearitem;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.wearitem.WearItemRequest;
import com.olie.api.dto.wearitem.WearItemResponse;
import com.olie.api.entity.Category;
import com.olie.api.entity.User;
import com.olie.api.entity.WearItem;
import com.olie.api.entity.WearItemCycle;
import com.olie.api.repository.WearItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateWearItemUseCase {

    private final WearItemRepository wearItemRepository;
    private final ResolveWearItemCategoryUseCase resolveWearItemCategoryUseCase;
    private final CalculateWearEstimateUseCase calculateWearEstimateUseCase;

    @Transactional
    public WearItemResponse execute(User user, UUID wearItemId, WearItemRequest request) {
        WearItem wearItem = wearItemRepository.findByIdAndUserId(wearItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wear item not found"));

        WearItemDateRules.requireInstallationNotBeforePurchase(request.purchaseDate(), request.installationDate());

        Category category = resolveWearItemCategoryUseCase.execute(user, request.categoryId());

        wearItem.setName(request.name());
        wearItem.setCategory(category);
        wearItem.setExpectedLifespan(request.expectedLifespan());
        wearItem.setExpectedLifespanUnit(request.expectedLifespanUnit());
        wearItem.setUpdatedAt(Instant.now());

        // o PUT corrige os dados da unidade em uso; uma unidade nova entra via /replace
        WearItemCycle currentCycle = wearItem.getCurrentCycle();
        currentCycle.setPurchaseDate(request.purchaseDate());
        currentCycle.setInstallationDate(request.installationDate());
        currentCycle.setPurchaseValue(request.purchaseValue());

        // dados usados pela estimativa mudaram: permite alertar novamente
        wearItem.setReplacementAlertSent(false);

        wearItemRepository.save(wearItem);

        return WearItemResponse.from(wearItem, calculateWearEstimateUseCase.execute(wearItem, LocalDate.now()));
    }
}

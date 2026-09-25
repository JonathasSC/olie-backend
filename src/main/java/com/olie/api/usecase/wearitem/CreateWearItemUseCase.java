package com.olie.api.usecase.wearitem;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CreateWearItemUseCase {

    private final WearItemRepository wearItemRepository;
    private final ResolveWearItemCategoryUseCase resolveWearItemCategoryUseCase;
    private final CalculateWearEstimateUseCase calculateWearEstimateUseCase;

    @Transactional
    public WearItemResponse execute(User user, WearItemRequest request) {
        WearItemDateRules.requireInstallationNotBeforePurchase(request.purchaseDate(), request.installationDate());

        Category category = resolveWearItemCategoryUseCase.execute(user, request.categoryId());

        WearItem wearItem = WearItem.builder()
                .user(user)
                .name(request.name())
                .category(category)
                .expectedLifespan(request.expectedLifespan())
                .expectedLifespanUnit(request.expectedLifespanUnit())
                .build();

        wearItem.addCycle(WearItemCycle.builder()
                .purchaseDate(request.purchaseDate())
                .installationDate(request.installationDate())
                .purchaseValue(request.purchaseValue())
                .build());

        wearItemRepository.save(wearItem);

        return WearItemResponse.from(wearItem, calculateWearEstimateUseCase.execute(wearItem, LocalDate.now()));
    }
}

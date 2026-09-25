package com.olie.api.dto.wearitem;

import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.WearItem;
import com.olie.api.entity.WearItemLifespanUnit;

public record WearItemResponse(
        UUID id,
        String name,
        UUID categoryId,
        int expectedLifespan,
        WearItemLifespanUnit expectedLifespanUnit,
        WearItemCycleResponse currentCycle,
        WearEstimateResponse estimate,
        int cyclesCount,
        Instant createdAt) {

    public static WearItemResponse from(WearItem wearItem, WearEstimateResponse estimate) {
        return new WearItemResponse(
                wearItem.getId(),
                wearItem.getName(),
                wearItem.getCategory() != null ? wearItem.getCategory().getId() : null,
                wearItem.getExpectedLifespan(),
                wearItem.getExpectedLifespanUnit(),
                WearItemCycleResponse.from(wearItem.getCurrentCycle()),
                estimate,
                wearItem.getCycles().size(),
                wearItem.getCreatedAt());
    }
}

package com.olie.api.dto.wearitem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.olie.api.entity.WearItem;
import com.olie.api.entity.WearItemLifespanUnit;

public record WearItemDetailResponse(
        UUID id,
        String name,
        UUID categoryId,
        int expectedLifespan,
        WearItemLifespanUnit expectedLifespanUnit,
        WearItemCycleResponse currentCycle,
        WearEstimateResponse estimate,
        int cyclesCount,
        Instant createdAt,
        List<WearItemHistoryEntryResponse> history) {

    public static WearItemDetailResponse from(WearItem wearItem, WearEstimateResponse estimate) {
        WearItemResponse summary = WearItemResponse.from(wearItem, estimate);

        return new WearItemDetailResponse(
                summary.id(),
                summary.name(),
                summary.categoryId(),
                summary.expectedLifespan(),
                summary.expectedLifespanUnit(),
                summary.currentCycle(),
                summary.estimate(),
                summary.cyclesCount(),
                summary.createdAt(),
                wearItem.getClosedCycles().stream().map(WearItemHistoryEntryResponse::from).toList());
    }
}

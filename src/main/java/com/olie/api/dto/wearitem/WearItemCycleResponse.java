package com.olie.api.dto.wearitem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.olie.api.entity.WearItemCycle;

public record WearItemCycleResponse(
        UUID id, LocalDate purchaseDate, LocalDate installationDate, BigDecimal purchaseValue) {

    public static WearItemCycleResponse from(WearItemCycle cycle) {
        return new WearItemCycleResponse(
                cycle.getId(), cycle.getPurchaseDate(), cycle.getInstallationDate(), cycle.getPurchaseValue());
    }
}

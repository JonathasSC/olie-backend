package com.olie.api.dto.wearitem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.olie.api.entity.WearItemCycle;

public record WearItemHistoryEntryResponse(
        UUID id,
        LocalDate purchaseDate,
        LocalDate installationDate,
        LocalDate removalDate,
        BigDecimal purchaseValue,
        Long lifespanDays) {

    public static WearItemHistoryEntryResponse from(WearItemCycle cycle) {
        Long lifespanDays = cycle.getInstallationDate() != null
                ? ChronoUnit.DAYS.between(cycle.getInstallationDate(), cycle.getRemovalDate())
                : null;

        return new WearItemHistoryEntryResponse(
                cycle.getId(),
                cycle.getPurchaseDate(),
                cycle.getInstallationDate(),
                cycle.getRemovalDate(),
                cycle.getPurchaseValue(),
                lifespanDays);
    }
}

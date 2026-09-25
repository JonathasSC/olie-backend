package com.olie.api.dto.wearitem;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.olie.api.entity.WearEstimateSource;
import com.olie.api.entity.WearItemStatus;

public record WearEstimateResponse(
        long lifespanDays,
        WearEstimateSource source,
        LocalDate estimatedReplacementDate,
        Long daysRemaining,
        Integer wearPercentage,
        WearItemStatus status,
        BigDecimal suggestedValue) {
}

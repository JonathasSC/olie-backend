package com.olie.api.usecase.wearitem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

import org.springframework.stereotype.Service;

import com.olie.api.dto.wearitem.WearEstimateResponse;
import com.olie.api.entity.WearEstimateSource;
import com.olie.api.entity.WearItem;
import com.olie.api.entity.WearItemCycle;
import com.olie.api.entity.WearItemStatus;

@Service
public class CalculateWearEstimateUseCase {

    static final long NEAR_END_DAYS = 15;
    static final int NEAR_END_WEAR_PERCENTAGE = 80;

    public WearEstimateResponse execute(WearItem wearItem, LocalDate today) {
        OptionalDouble averageLifespan = wearItem.getClosedCycles().stream()
                .filter(cycle -> cycle.getInstallationDate() != null)
                .mapToLong(cycle -> ChronoUnit.DAYS.between(cycle.getInstallationDate(), cycle.getRemovalDate()))
                .average();

        WearEstimateSource source = averageLifespan.isPresent() ? WearEstimateSource.HISTORY : WearEstimateSource.EXPECTED;
        long lifespanDays = averageLifespan.isPresent()
                ? Math.round(averageLifespan.getAsDouble())
                : wearItem.getExpectedLifespanUnit().toDays(wearItem.getExpectedLifespan());
        // evita divisão por zero quando o histórico só tem unidades removidas no mesmo dia da instalação
        lifespanDays = Math.max(lifespanDays, 1);

        BigDecimal suggestedValue = suggestedValue(wearItem.getCycles());
        LocalDate installationDate = wearItem.getCurrentCycle().getInstallationDate();

        if (installationDate == null) {
            return new WearEstimateResponse(
                    lifespanDays, source, null, null, null, WearItemStatus.IN_STOCK, suggestedValue);
        }

        LocalDate estimatedReplacementDate = installationDate.plusDays(lifespanDays);
        long daysRemaining = ChronoUnit.DAYS.between(today, estimatedReplacementDate);
        long daysInUse = ChronoUnit.DAYS.between(installationDate, today);
        int wearPercentage = (int) Math.round(daysInUse * 100.0 / lifespanDays);

        return new WearEstimateResponse(
                lifespanDays,
                source,
                estimatedReplacementDate,
                daysRemaining,
                wearPercentage,
                status(daysRemaining, wearPercentage),
                suggestedValue);
    }

    private WearItemStatus status(long daysRemaining, int wearPercentage) {
        if (daysRemaining < 0) {
            return WearItemStatus.OVERDUE;
        }
        if (daysRemaining <= NEAR_END_DAYS || wearPercentage >= NEAR_END_WEAR_PERCENTAGE) {
            return WearItemStatus.NEAR_END;
        }
        return WearItemStatus.OK;
    }

    private BigDecimal suggestedValue(List<WearItemCycle> cycles) {
        List<BigDecimal> values = cycles.stream()
                .map(WearItemCycle::getPurchaseValue)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return null;
        }

        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
}

package com.olie.api.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.wearitem.WearEstimateResponse;
import com.olie.api.entity.WearItem;
import com.olie.api.entity.WearItemStatus;
import com.olie.api.notification.NotificationEvent;
import com.olie.api.notification.NotificationEventProducer;
import com.olie.api.repository.WearItemRepository;
import com.olie.api.usecase.wearitem.CalculateWearEstimateUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WearItemNotificationScheduler {

    private final WearItemRepository wearItemRepository;
    private final CalculateWearEstimateUseCase calculateWearEstimateUseCase;
    private final NotificationEventProducer notificationEventProducer;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkWearItems() {
        LocalDate today = LocalDate.now();
        List<WearItem> wearItems = wearItemRepository.findAllByReplacementAlertSentFalse();

        for (WearItem wearItem : wearItems) {
            WearEstimateResponse estimate = calculateWearEstimateUseCase.execute(wearItem, today);

            if (estimate.status() == WearItemStatus.NEAR_END || estimate.status() == WearItemStatus.OVERDUE) {
                notificationEventProducer.publish(
                        NotificationEvent.wearItemReplacementApproaching(wearItem, estimate.estimatedReplacementDate()));
                wearItem.setReplacementAlertSent(true);
            }
        }
    }
}

package com.olie.api.scheduler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.TransactionType;
import com.olie.api.notification.NotificationEvent;
import com.olie.api.notification.NotificationEventProducer;
import com.olie.api.notification.NotificationProperties;
import com.olie.api.repository.PlannedItemRepository;
import com.olie.api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlannedItemNotificationScheduler {

    private final PlannedItemRepository plannedItemRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationEventProducer notificationEventProducer;
    private final NotificationProperties notificationProperties;

    @Scheduled(cron = "0 0 * * * *")
    public void checkPlannedItems() {
        checkApproachingDates();
        checkSufficientBalance();
    }

    private void checkApproachingDates() {
        LocalDate threshold = LocalDate.now().plusDays(notificationProperties.purchaseDateApproachingDays());

        List<PlannedItem> plannedItems =
                plannedItemRepository.findAllByDateAlertSentFalseAndEstimatedDateLessThanEqual(threshold);

        for (PlannedItem plannedItem : plannedItems) {
            notificationEventProducer.publish(NotificationEvent.purchaseDateApproaching(plannedItem));
            plannedItem.setDateAlertSent(true);
        }

        plannedItemRepository.saveAll(plannedItems);
    }

    private void checkSufficientBalance() {
        List<PlannedItem> plannedItems = plannedItemRepository.findAllByBalanceAlertSentFalseAndCategoryIsNotNull();

        for (PlannedItem plannedItem : plannedItems) {
            BigDecimal balance = categoryBalance(plannedItem.getUser().getId(), plannedItem.getCategory().getId());

            if (balance.compareTo(plannedItem.getEstimatedValue()) >= 0) {
                notificationEventProducer.publish(NotificationEvent.sufficientBalance(plannedItem));
                plannedItem.setBalanceAlertSent(true);
            }
        }

        plannedItemRepository.saveAll(plannedItems);
    }

    private BigDecimal categoryBalance(UUID userId, UUID categoryId) {
        BigDecimal income = transactionRepository.sumValueByUserIdAndCategoryIdAndType(
                userId, categoryId, TransactionType.INCOME);
        BigDecimal expense = transactionRepository.sumValueByUserIdAndCategoryIdAndType(
                userId, categoryId, TransactionType.EXPENSE);
        return income.subtract(expense);
    }
}

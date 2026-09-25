package com.olie.api.usecase.wearitem;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.wearitem.ReplaceWearItemRequest;
import com.olie.api.dto.wearitem.WearItemResponse;
import com.olie.api.entity.Transaction;
import com.olie.api.entity.TransactionType;
import com.olie.api.entity.User;
import com.olie.api.entity.WearItem;
import com.olie.api.entity.WearItemCycle;
import com.olie.api.repository.TransactionRepository;
import com.olie.api.repository.WearItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReplaceWearItemUseCase {

    private final WearItemRepository wearItemRepository;
    private final TransactionRepository transactionRepository;
    private final CalculateWearEstimateUseCase calculateWearEstimateUseCase;

    @Transactional
    public WearItemResponse execute(User user, UUID wearItemId, ReplaceWearItemRequest request) {
        WearItem wearItem = wearItemRepository.findByIdAndUserId(wearItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wear item not found"));

        WearItemDateRules.requireInstallationNotBeforePurchase(request.purchaseDate(), request.installationDate());

        boolean registerTransaction = request.paymentMethod() != null && !request.paymentMethod().isBlank();
        if (registerTransaction && request.purchaseValue() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "purchaseValue is required when paymentMethod is informed");
        }

        LocalDate today = LocalDate.now();
        WearItemCycle currentCycle = wearItem.getCurrentCycle();
        LocalDate removalDate = request.removalDate() != null ? request.removalDate()
                : request.installationDate() != null ? request.installationDate()
                : today;

        LocalDate currentCycleStart = currentCycle.getInstallationDate() != null
                ? currentCycle.getInstallationDate()
                : currentCycle.getPurchaseDate();
        if (removalDate.isBefore(currentCycleStart)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "removalDate must not be before the current cycle installation");
        }

        currentCycle.setRemovalDate(removalDate);
        // o índice único de ciclo atual exige encerrar o ciclo antigo antes de inserir o novo
        wearItemRepository.saveAndFlush(wearItem);

        wearItem.addCycle(WearItemCycle.builder()
                .purchaseDate(request.purchaseDate())
                .installationDate(request.installationDate())
                .purchaseValue(request.purchaseValue())
                .build());
        wearItem.setReplacementAlertSent(false);
        wearItem.setUpdatedAt(Instant.now());

        wearItemRepository.save(wearItem);

        if (registerTransaction) {
            transactionRepository.save(Transaction.builder()
                    .user(user)
                    .value(request.purchaseValue())
                    .date(request.purchaseDate())
                    .paymentMethod(request.paymentMethod())
                    .category(wearItem.getCategory())
                    .type(TransactionType.EXPENSE)
                    .build());
        }

        return WearItemResponse.from(wearItem, calculateWearEstimateUseCase.execute(wearItem, today));
    }
}

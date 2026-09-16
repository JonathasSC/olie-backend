package com.olie.api.usecase.planneditem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.planneditem.CompletePlannedItemRequest;
import com.olie.api.dto.transaction.TransactionResponse;
import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.Transaction;
import com.olie.api.entity.TransactionType;
import com.olie.api.entity.User;
import com.olie.api.repository.PlannedItemRepository;
import com.olie.api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompletePlannedItemUseCase {

    private final PlannedItemRepository plannedItemRepository;
    private final TransactionRepository transactionRepository;

    public TransactionResponse execute(User user, UUID plannedItemId, CompletePlannedItemRequest request) {
        PlannedItem plannedItem = plannedItemRepository.findByIdAndUserId(plannedItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Planned item not found"));

        BigDecimal value = request.value() != null ? request.value() : plannedItem.getEstimatedValue();

        Transaction transaction = Transaction.builder()
                .user(user)
                .value(value)
                .date(LocalDate.now())
                .paymentMethod(request.paymentMethod())
                .category(plannedItem.getCategory())
                .type(TransactionType.EXPENSE)
                .build();

        transactionRepository.save(transaction);
        plannedItemRepository.delete(plannedItem);

        return TransactionResponse.from(transaction);
    }
}

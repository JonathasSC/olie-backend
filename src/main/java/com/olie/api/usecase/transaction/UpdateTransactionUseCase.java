package com.olie.api.usecase.transaction;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.transaction.TransactionRequest;
import com.olie.api.dto.transaction.TransactionResponse;
import com.olie.api.entity.Category;
import com.olie.api.entity.Transaction;
import com.olie.api.entity.User;
import com.olie.api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final ResolveTransactionCategoryUseCase resolveTransactionCategoryUseCase;

    public TransactionResponse execute(User user, UUID transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        Category category = resolveTransactionCategoryUseCase.execute(user, request.categoryId());

        transaction.setValue(request.value());
        transaction.setPaymentMethod(request.paymentMethod());
        transaction.setType(request.type());
        transaction.setCategory(category);
        transaction.setUpdatedAt(Instant.now());

        transactionRepository.save(transaction);

        return TransactionResponse.from(transaction);
    }
}

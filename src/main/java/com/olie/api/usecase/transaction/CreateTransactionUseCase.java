package com.olie.api.usecase.transaction;

import org.springframework.stereotype.Service;

import com.olie.api.dto.transaction.TransactionRequest;
import com.olie.api.dto.transaction.TransactionResponse;
import com.olie.api.entity.Category;
import com.olie.api.entity.Transaction;
import com.olie.api.entity.User;
import com.olie.api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final ResolveTransactionCategoryUseCase resolveTransactionCategoryUseCase;

    public TransactionResponse execute(User user, TransactionRequest request) {
        Category category = resolveTransactionCategoryUseCase.execute(user, request.categoryId());

        Transaction transaction = Transaction.builder()
                .user(user)
                .value(request.value())
                .date(request.date())
                .paymentMethod(request.paymentMethod())
                .type(request.type())
                .category(category)
                .build();

        transactionRepository.save(transaction);

        return TransactionResponse.from(transaction);
    }
}

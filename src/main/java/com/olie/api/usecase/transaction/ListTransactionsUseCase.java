package com.olie.api.usecase.transaction;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olie.api.dto.transaction.TransactionResponse;
import com.olie.api.entity.User;
import com.olie.api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public List<TransactionResponse> execute(User user) {
        return transactionRepository.findAllByUserId(user.getId()).stream()
                .map(TransactionResponse::from)
                .toList();
    }
}

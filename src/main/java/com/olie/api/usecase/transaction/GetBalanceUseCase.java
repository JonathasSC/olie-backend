package com.olie.api.usecase.transaction;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.olie.api.dto.transaction.BalanceResponse;
import com.olie.api.entity.TransactionType;
import com.olie.api.entity.User;
import com.olie.api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetBalanceUseCase {

    private final TransactionRepository transactionRepository;

    public BalanceResponse execute(User user) {
        BigDecimal income = transactionRepository.sumValueByUserIdAndType(user.getId(), TransactionType.INCOME);
        BigDecimal expense = transactionRepository.sumValueByUserIdAndType(user.getId(), TransactionType.EXPENSE);

        return new BalanceResponse(income.subtract(expense));
    }
}

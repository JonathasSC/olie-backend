package com.olie.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.transaction.BalanceResponse;
import com.olie.api.dto.transaction.TransactionRequest;
import com.olie.api.dto.transaction.TransactionResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.transaction.CreateTransactionUseCase;
import com.olie.api.usecase.transaction.DeleteTransactionUseCase;
import com.olie.api.usecase.transaction.GetBalanceUseCase;
import com.olie.api.usecase.transaction.ListTransactionsUseCase;
import com.olie.api.usecase.transaction.UpdateTransactionUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final GetBalanceUseCase getBalanceUseCase;

    @GetMapping
    public List<TransactionResponse> list(@AuthenticationPrincipal User user) {
        return listTransactionsUseCase.execute(user);
    }

    @GetMapping("/balance")
    public BalanceResponse balance(@AuthenticationPrincipal User user) {
        return getBalanceUseCase.execute(user);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createTransactionUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest request) {
        return updateTransactionUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deleteTransactionUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }
}

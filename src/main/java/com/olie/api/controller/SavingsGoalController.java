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

import com.olie.api.dto.savingsgoal.SavingsGoalRequest;
import com.olie.api.dto.savingsgoal.SavingsGoalResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.savingsgoal.CreateSavingsGoalUseCase;
import com.olie.api.usecase.savingsgoal.DeleteSavingsGoalUseCase;
import com.olie.api.usecase.savingsgoal.ListSavingsGoalsUseCase;
import com.olie.api.usecase.savingsgoal.UpdateSavingsGoalUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final CreateSavingsGoalUseCase createSavingsGoalUseCase;
    private final UpdateSavingsGoalUseCase updateSavingsGoalUseCase;
    private final DeleteSavingsGoalUseCase deleteSavingsGoalUseCase;
    private final ListSavingsGoalsUseCase listSavingsGoalsUseCase;

    @GetMapping
    public List<SavingsGoalResponse> list(@AuthenticationPrincipal User user) {
        return listSavingsGoalsUseCase.execute(user);
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createSavingsGoalUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public SavingsGoalResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody SavingsGoalRequest request) {
        return updateSavingsGoalUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deleteSavingsGoalUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }
}

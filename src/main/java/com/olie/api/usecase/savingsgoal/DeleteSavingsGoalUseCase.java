package com.olie.api.usecase.savingsgoal;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.SavingsGoal;
import com.olie.api.entity.User;
import com.olie.api.repository.SavingsGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteSavingsGoalUseCase {

    private final SavingsGoalRepository savingsGoalRepository;

    public void execute(User user, UUID savingsGoalId) {
        SavingsGoal savingsGoal = savingsGoalRepository.findByIdAndUserId(savingsGoalId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings goal not found"));

        savingsGoalRepository.delete(savingsGoal);
    }
}

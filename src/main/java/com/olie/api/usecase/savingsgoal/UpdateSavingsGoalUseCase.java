package com.olie.api.usecase.savingsgoal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.savingsgoal.SavingsGoalRequest;
import com.olie.api.dto.savingsgoal.SavingsGoalResponse;
import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.SavingsGoal;
import com.olie.api.entity.User;
import com.olie.api.repository.SavingsGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateSavingsGoalUseCase {

    private final SavingsGoalRepository savingsGoalRepository;
    private final ResolveSavingsGoalPlannedItemUseCase resolveSavingsGoalPlannedItemUseCase;

    public SavingsGoalResponse execute(User user, UUID savingsGoalId, SavingsGoalRequest request) {
        SavingsGoal savingsGoal = savingsGoalRepository.findByIdAndUserId(savingsGoalId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings goal not found"));

        PlannedItem plannedItem = resolveSavingsGoalPlannedItemUseCase.execute(user, request.plannedItemId());

        savingsGoal.setName(request.name());
        savingsGoal.setAmount(request.amount());
        savingsGoal.setPeriod(request.period());
        savingsGoal.setPlannedItem(plannedItem);
        savingsGoal.setUpdatedAt(Instant.now());

        savingsGoalRepository.save(savingsGoal);

        return SavingsGoalResponse.from(savingsGoal);
    }
}

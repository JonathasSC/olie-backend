package com.olie.api.usecase.savingsgoal;

import org.springframework.stereotype.Service;

import com.olie.api.dto.savingsgoal.SavingsGoalRequest;
import com.olie.api.dto.savingsgoal.SavingsGoalResponse;
import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.SavingsGoal;
import com.olie.api.entity.User;
import com.olie.api.repository.SavingsGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateSavingsGoalUseCase {

    private final SavingsGoalRepository savingsGoalRepository;
    private final ResolveSavingsGoalPlannedItemUseCase resolveSavingsGoalPlannedItemUseCase;

    public SavingsGoalResponse execute(User user, SavingsGoalRequest request) {
        PlannedItem plannedItem = resolveSavingsGoalPlannedItemUseCase.execute(user, request.plannedItemId());

        SavingsGoal savingsGoal = SavingsGoal.builder()
                .user(user)
                .name(request.name())
                .amount(request.amount())
                .period(request.period())
                .plannedItem(plannedItem)
                .build();

        savingsGoalRepository.save(savingsGoal);

        return SavingsGoalResponse.from(savingsGoal);
    }
}

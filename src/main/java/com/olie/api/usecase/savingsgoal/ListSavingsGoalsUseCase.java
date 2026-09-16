package com.olie.api.usecase.savingsgoal;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olie.api.dto.savingsgoal.SavingsGoalResponse;
import com.olie.api.entity.User;
import com.olie.api.repository.SavingsGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListSavingsGoalsUseCase {

    private final SavingsGoalRepository savingsGoalRepository;

    public List<SavingsGoalResponse> execute(User user) {
        return savingsGoalRepository.findAllByUserId(user.getId()).stream()
                .map(SavingsGoalResponse::from)
                .toList();
    }
}

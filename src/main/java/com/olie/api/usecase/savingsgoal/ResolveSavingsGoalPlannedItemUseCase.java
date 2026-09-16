package com.olie.api.usecase.savingsgoal;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.User;
import com.olie.api.repository.PlannedItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveSavingsGoalPlannedItemUseCase {

    private final PlannedItemRepository plannedItemRepository;

    public PlannedItem execute(User user, UUID plannedItemId) {
        if (plannedItemId == null) {
            return null;
        }

        return plannedItemRepository.findByIdAndUserId(plannedItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Planned item not found"));
    }
}

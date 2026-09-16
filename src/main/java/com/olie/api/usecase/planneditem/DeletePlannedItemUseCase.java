package com.olie.api.usecase.planneditem;

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
public class DeletePlannedItemUseCase {

    private final PlannedItemRepository plannedItemRepository;

    public void execute(User user, UUID plannedItemId) {
        PlannedItem plannedItem = plannedItemRepository.findByIdAndUserId(plannedItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Planned item not found"));

        plannedItemRepository.delete(plannedItem);
    }
}

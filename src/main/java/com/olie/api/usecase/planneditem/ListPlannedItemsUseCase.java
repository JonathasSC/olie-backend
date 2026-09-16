package com.olie.api.usecase.planneditem;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olie.api.dto.planneditem.PlannedItemResponse;
import com.olie.api.entity.User;
import com.olie.api.repository.PlannedItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListPlannedItemsUseCase {

    private final PlannedItemRepository plannedItemRepository;

    public List<PlannedItemResponse> execute(User user) {
        return plannedItemRepository.findAllByUserId(user.getId()).stream()
                .map(PlannedItemResponse::from)
                .toList();
    }
}

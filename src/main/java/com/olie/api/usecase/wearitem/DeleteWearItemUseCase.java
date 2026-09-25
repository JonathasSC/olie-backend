package com.olie.api.usecase.wearitem;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.User;
import com.olie.api.entity.WearItem;
import com.olie.api.repository.WearItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteWearItemUseCase {

    private final WearItemRepository wearItemRepository;

    @Transactional
    public void execute(User user, UUID wearItemId) {
        WearItem wearItem = wearItemRepository.findByIdAndUserId(wearItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wear item not found"));

        wearItemRepository.delete(wearItem);
    }
}

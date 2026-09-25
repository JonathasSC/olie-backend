package com.olie.api.usecase.wearitem;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.wearitem.WearItemDetailResponse;
import com.olie.api.entity.User;
import com.olie.api.entity.WearItem;
import com.olie.api.repository.WearItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetWearItemUseCase {

    private final WearItemRepository wearItemRepository;
    private final CalculateWearEstimateUseCase calculateWearEstimateUseCase;

    @Transactional(readOnly = true)
    public WearItemDetailResponse execute(User user, UUID wearItemId) {
        WearItem wearItem = wearItemRepository.findByIdAndUserId(wearItemId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wear item not found"));

        return WearItemDetailResponse.from(wearItem, calculateWearEstimateUseCase.execute(wearItem, LocalDate.now()));
    }
}

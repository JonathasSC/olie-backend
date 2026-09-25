package com.olie.api.usecase.wearitem;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.wearitem.WearItemResponse;
import com.olie.api.entity.User;
import com.olie.api.entity.WearItemStatus;
import com.olie.api.repository.WearItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListWearItemsUseCase {

    private static final Comparator<WearItemResponse> BY_REPLACEMENT_DATE = Comparator.comparing(
            (WearItemResponse item) -> item.estimate().estimatedReplacementDate(),
            Comparator.nullsLast(Comparator.naturalOrder()));

    private final WearItemRepository wearItemRepository;
    private final CalculateWearEstimateUseCase calculateWearEstimateUseCase;

    @Transactional(readOnly = true)
    public List<WearItemResponse> execute(User user, List<WearItemStatus> statuses, UUID categoryId) {
        LocalDate today = LocalDate.now();

        return wearItemRepository.findAllByUserId(user.getId()).stream()
                .filter(wearItem -> categoryId == null
                        || (wearItem.getCategory() != null && wearItem.getCategory().getId().equals(categoryId)))
                .map(wearItem -> WearItemResponse.from(wearItem, calculateWearEstimateUseCase.execute(wearItem, today)))
                .filter(item -> statuses == null || statuses.isEmpty() || statuses.contains(item.estimate().status()))
                .sorted(BY_REPLACEMENT_DATE)
                .toList();
    }
}

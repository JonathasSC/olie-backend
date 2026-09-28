package com.olie.api.usecase.inquiry;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquirySummaryResponse;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.User;
import com.olie.api.inquiry.InquiryProperties;
import com.olie.api.repository.InquiryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListInquiriesUseCase {

    private final InquiryRepository inquiryRepository;
    private final InquiryProperties properties;

    /**
     * Histórico, mais recentes primeiro. {@code from}/{@code to} são datas no fuso configurado (inclusive);
     * {@code item} busca por parte do nome, sem diferenciar maiúsculas.
     */
    @Transactional(readOnly = true)
    public List<InquirySummaryResponse> execute(
            User user, LocalDate from, LocalDate to, UUID categoryId, String item, Boolean simulated) {
        ZoneId zone = properties.timezone();
        Instant start = from != null ? from.atStartOfDay(zone).toInstant() : null;
        Instant end = to != null ? to.plusDays(1).atStartOfDay(zone).toInstant() : null;
        String itemQuery = item != null && !item.isBlank() ? item.trim().toLowerCase(Locale.ROOT) : null;

        return inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(inquiry -> start == null || !inquiry.getCreatedAt().isBefore(start))
                .filter(inquiry -> end == null || inquiry.getCreatedAt().isBefore(end))
                .filter(inquiry -> simulated == null || inquiry.isSimulated() == simulated)
                .filter(inquiry -> categoryId == null || inquiry.getCategories().stream()
                        .anyMatch(category -> category.getId().equals(categoryId)))
                .filter(inquiry -> itemQuery == null || matchesItem(inquiry, itemQuery))
                .map(InquirySummaryResponse::from)
                .toList();
    }

    private static boolean matchesItem(Inquiry inquiry, String itemQuery) {
        return inquiry.getItems().stream()
                .anyMatch(inquiryItem -> inquiryItem.getName().toLowerCase(Locale.ROOT).contains(itemQuery));
    }
}

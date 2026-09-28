package com.olie.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.inquiry.CreateInquiryRequest;
import com.olie.api.dto.inquiry.InquiryDraftResponse;
import com.olie.api.dto.inquiry.InquiryPreviewResponse;
import com.olie.api.dto.inquiry.InquiryResponse;
import com.olie.api.dto.inquiry.InquirySettingsResponse;
import com.olie.api.dto.inquiry.InquirySummaryResponse;
import com.olie.api.dto.inquiry.PreviewInquiryRequest;
import com.olie.api.entity.User;
import com.olie.api.usecase.inquiry.CancelInquiryUseCase;
import com.olie.api.usecase.inquiry.CreateInquiryUseCase;
import com.olie.api.usecase.inquiry.GetInquiryDraftUseCase;
import com.olie.api.usecase.inquiry.GetInquirySettingsUseCase;
import com.olie.api.usecase.inquiry.GetInquiryUseCase;
import com.olie.api.usecase.inquiry.ListInquiriesUseCase;
import com.olie.api.usecase.inquiry.PauseInquiryUseCase;
import com.olie.api.usecase.inquiry.PreviewInquiryUseCase;
import com.olie.api.usecase.inquiry.ResendFailedInquiryUseCase;
import com.olie.api.usecase.inquiry.ResumeInquiryUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final ListInquiriesUseCase listInquiriesUseCase;
    private final GetInquiryUseCase getInquiryUseCase;
    private final GetInquiryDraftUseCase getInquiryDraftUseCase;
    private final GetInquirySettingsUseCase getInquirySettingsUseCase;
    private final PreviewInquiryUseCase previewInquiryUseCase;
    private final CreateInquiryUseCase createInquiryUseCase;
    private final PauseInquiryUseCase pauseInquiryUseCase;
    private final ResumeInquiryUseCase resumeInquiryUseCase;
    private final CancelInquiryUseCase cancelInquiryUseCase;
    private final ResendFailedInquiryUseCase resendFailedInquiryUseCase;

    @GetMapping
    public List<InquirySummaryResponse> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String item,
            @RequestParam(required = false) Boolean simulated) {
        return listInquiriesUseCase.execute(user, from, to, categoryId, item, simulated);
    }

    @GetMapping("/settings")
    public InquirySettingsResponse settings() {
        return getInquirySettingsUseCase.execute();
    }

    @PostMapping("/preview")
    public InquiryPreviewResponse preview(@Valid @RequestBody PreviewInquiryRequest request) {
        return previewInquiryUseCase.execute(request);
    }

    @PostMapping
    public ResponseEntity<InquiryResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody CreateInquiryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createInquiryUseCase.execute(user, request));
    }

    @GetMapping("/{id}")
    public InquiryResponse get(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return getInquiryUseCase.execute(user, id);
    }

    @GetMapping("/{id}/draft")
    public InquiryDraftResponse draft(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return getInquiryDraftUseCase.execute(user, id);
    }

    @PostMapping("/{id}/pause")
    public InquiryResponse pause(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return pauseInquiryUseCase.execute(user, id);
    }

    @PostMapping("/{id}/resume")
    public InquiryResponse resume(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return resumeInquiryUseCase.execute(user, id);
    }

    @PostMapping("/{id}/cancel")
    public InquiryResponse cancel(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return cancelInquiryUseCase.execute(user, id);
    }

    @PostMapping("/{id}/resend-failed")
    public InquiryResponse resendFailed(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return resendFailedInquiryUseCase.execute(user, id);
    }
}

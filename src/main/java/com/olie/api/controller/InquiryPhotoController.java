package com.olie.api.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.olie.api.dto.inquiry.InquiryPhotoResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.inquiry.GetInquiryPhotoUseCase;
import com.olie.api.usecase.inquiry.UploadInquiryPhotoUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/inquiry-photos")
@RequiredArgsConstructor
public class InquiryPhotoController {

    private final UploadInquiryPhotoUseCase uploadInquiryPhotoUseCase;
    private final GetInquiryPhotoUseCase getInquiryPhotoUseCase;

    @PostMapping
    public ResponseEntity<InquiryPhotoResponse> upload(
            @AuthenticationPrincipal User user, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadInquiryPhotoUseCase.execute(user, file.getBytes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        GetInquiryPhotoUseCase.PhotoContent photo = getInquiryPhotoUseCase.execute(user, id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                // a foto nunca muda depois de enviada
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePrivate())
                .body(photo.content());
    }
}

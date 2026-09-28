package com.olie.api.usecase.inquiry;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.InquiryPhoto;
import com.olie.api.entity.User;
import com.olie.api.inquiry.InquiryPhotoStorage;
import com.olie.api.repository.InquiryPhotoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInquiryPhotoUseCase {

    public record PhotoContent(byte[] content, String contentType) {
    }

    private final InquiryPhotoRepository inquiryPhotoRepository;
    private final InquiryPhotoStorage inquiryPhotoStorage;

    @Transactional(readOnly = true)
    public PhotoContent execute(User user, UUID photoId) {
        InquiryPhoto photo = inquiryPhotoRepository.findByIdAndUserId(photoId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));

        return new PhotoContent(inquiryPhotoStorage.read(photo.getStoragePath()), photo.getContentType());
    }
}

package com.olie.api.usecase.inquiry;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquiryPhotoResponse;
import com.olie.api.entity.InquiryPhoto;
import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.inquiry.InquiryPhotoProcessor;
import com.olie.api.inquiry.InquiryPhotoStorage;
import com.olie.api.inquiry.InquiryProperties;
import com.olie.api.inquiry.ProcessedPhoto;
import com.olie.api.repository.InquiryPhotoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadInquiryPhotoUseCase {

    private final InquiryPhotoRepository inquiryPhotoRepository;
    private final InquiryPhotoProcessor inquiryPhotoProcessor;
    private final InquiryPhotoStorage inquiryPhotoStorage;
    private final InquiryProperties properties;

    @Transactional
    public InquiryPhotoResponse execute(User user, byte[] content) {
        if (content.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_PHOTO", "Nenhuma imagem foi enviada.");
        }
        if (content.length > properties.photos().maxUploadSize().toBytes()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PHOTO_TOO_LARGE",
                    "A imagem passa do limite de %d MB.".formatted(properties.photos().maxUploadSize().toMegabytes()));
        }

        ProcessedPhoto processed = inquiryPhotoProcessor.process(content);

        InquiryPhoto photo = InquiryPhoto.builder()
                .user(user)
                .contentType(processed.contentType())
                .sizeBytes(processed.content().length)
                .width(processed.width())
                .height(processed.height())
                .build();
        photo.setStoragePath(
                inquiryPhotoStorage.save(user.getId(), photo.getId(), processed.extension(), processed.content()));
        inquiryPhotoRepository.save(photo);

        return InquiryPhotoResponse.from(photo);
    }
}

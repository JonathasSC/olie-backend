package com.olie.api.dto.inquiry;

import java.util.UUID;

import com.olie.api.controller.ApiV1;
import com.olie.api.entity.InquiryPhoto;

public record InquiryPhotoResponse(UUID id, String contentType, long sizeBytes, int width, int height, String url) {

    public static String url(UUID photoId) {
        return ApiV1.PREFIX + "/inquiry-photos/" + photoId;
    }

    public static InquiryPhotoResponse from(InquiryPhoto photo) {
        return new InquiryPhotoResponse(photo.getId(), photo.getContentType(), photo.getSizeBytes(),
                photo.getWidth(), photo.getHeight(), url(photo.getId()));
    }
}

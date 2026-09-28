package com.olie.api.dto.inquiry;

import java.util.UUID;

import com.olie.api.entity.InquiryItem;

public record InquiryItemResponse(UUID id, int position, String name, UUID photoId, String photoUrl) {

    public static InquiryItemResponse from(InquiryItem item) {
        UUID photoId = item.getPhoto() != null ? item.getPhoto().getId() : null;
        return new InquiryItemResponse(item.getId(), item.getPosition(), item.getName(), photoId,
                photoId != null ? InquiryPhotoResponse.url(photoId) : null);
    }
}

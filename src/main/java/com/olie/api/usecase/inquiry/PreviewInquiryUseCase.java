package com.olie.api.usecase.inquiry;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.olie.api.dto.inquiry.InquiryItemRequest;
import com.olie.api.dto.inquiry.InquiryPreviewResponse;
import com.olie.api.dto.inquiry.InquiryPreviewResponse.Message;
import com.olie.api.dto.inquiry.InquiryPreviewResponse.MessageType;
import com.olie.api.dto.inquiry.PreviewInquiryRequest;
import com.olie.api.exception.ApiException;
import com.olie.api.inquiry.ComposedMessage;
import com.olie.api.inquiry.InquiryMessageComposer;
import com.olie.api.inquiry.InquiryProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreviewInquiryUseCase {

    private final InquiryMessageComposer inquiryMessageComposer;
    private final InquiryProperties properties;

    public InquiryPreviewResponse execute(PreviewInquiryRequest request) {
        if (request.items().size() > properties.maxItems()) {
            throw tooManyItems(properties.maxItems());
        }

        List<InquiryItemRequest> items = request.items();
        ComposedMessage composed = inquiryMessageComposer.compose(items.stream().map(InquiryItemRequest::name).toList());

        List<Message> messages = new ArrayList<>();
        messages.add(new Message(MessageType.TEXT, composed.text(), null));
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index).photoId() != null) {
                messages.add(new Message(MessageType.PHOTO, composed.itemCaptions().get(index), items.get(index).photoId()));
            }
        }

        return new InquiryPreviewResponse(composed.greeting(), composed.question(), messages);
    }

    static ApiException tooManyItems(int maxItems) {
        return new ApiException(HttpStatus.BAD_REQUEST, "TOO_MANY_ITEMS",
                "Uma consulta pode ter no máximo %d itens.".formatted(maxItems));
    }
}

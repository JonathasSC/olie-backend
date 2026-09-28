package com.olie.api.usecase.inquiry;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquiryResponse;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.InquiryRecipientStatus;
import com.olie.api.entity.InquiryStatus;
import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResendFailedInquiryUseCase {

    private final FindUserInquiryUseCase findUserInquiryUseCase;
    private final InquiryControlSupport inquiryControlSupport;

    /**
     * Devolve à fila só os contatos que falharam. O que cada um já recebeu é preservado ({@code sentParts}),
     * então o reenvio continua de onde parou em vez de repetir o texto.
     */
    @Transactional
    public InquiryResponse execute(User user, UUID inquiryId) {
        Inquiry inquiry = findUserInquiryUseCase.execute(user, inquiryId);
        inquiryControlSupport.requireStatus(
                inquiry, InquiryStatus.COMPLETED, InquiryStatus.PAUSED, InquiryStatus.CANCELLED);

        List<InquiryRecipient> failed = inquiry.getRecipients().stream()
                .filter(recipient -> recipient.getStatus() == InquiryRecipientStatus.FAILED)
                .toList();
        if (failed.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_FAILED_RECIPIENTS",
                    "Nenhum contato falhou nesta consulta.");
        }
        inquiryControlSupport.requireConnected(user, inquiry);

        for (InquiryRecipient recipient : failed) {
            recipient.setStatus(InquiryRecipientStatus.PENDING);
            recipient.setFailureReason(null);
        }
        inquiry.setFinishedAt(null);
        inquiryControlSupport.restart(inquiry);

        return inquiryControlSupport.publish(user, inquiry);
    }
}

package com.olie.api.inquiry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.olie.api.whatsapp.SimulatedWhatsAppProvider;
import com.olie.api.whatsapp.WhatsAppProvider;
import com.olie.api.whatsapp.WhatsAppSendException;
import com.olie.api.whatsapp.WhatsAppSendException.Kind;

class InquiryDispatcherTest {

    private final InquirySendingService sendingService = mock(InquirySendingService.class);
    private final WhatsAppProvider provider = mock(WhatsAppProvider.class);
    private final InquiryPhotoStorage storage = mock(InquiryPhotoStorage.class);
    private final InquiryProperties properties = InquiryTestProperties.create(Path.of("unused"));
    private final InquiryDispatcher dispatcher = new InquiryDispatcher(sendingService, provider, storage, properties);

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void photoExists() {
        when(storage.read(anyString())).thenReturn(new byte[] {1, 2, 3});
    }

    private SendJob job(boolean simulated, int sentParts) {
        return new SendJob(UUID.randomUUID(), UUID.randomUUID(), userId, "+5511999999999", simulated,
                List.of(new SendJob.Part("Bom dia! Tem esse item?", null, null),
                        new SendJob.Part("1. Item", "u/p.jpg", "image/jpeg")),
                sentParts);
    }

    @Test
    void sendsTextThenPhoto() {
        SendOutcome outcome = dispatcher.send(job(false, 0));

        assertThat(outcome.result()).isEqualTo(SendOutcome.Result.SENT);
        verify(provider).sendText(userId, "+5511999999999", "Bom dia! Tem esse item?");
        verify(provider).sendImage(any(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void retriesTransientFailuresAndResumesFromLastDeliveredPart() {
        doNothing().when(provider).sendText(any(), anyString(), anyString());
        doThrow(new WhatsAppSendException(Kind.TRANSIENT, "timeout"))
                .doNothing()
                .when(provider).sendImage(any(), anyString(), any(), anyString(), anyString());

        SendOutcome outcome = dispatcher.send(job(false, 0));

        assertThat(outcome.result()).isEqualTo(SendOutcome.Result.SENT);
        assertThat(outcome.attempts()).isEqualTo(2);
        // o texto saiu na primeira tentativa e não é repetido na segunda
        verify(provider, times(1)).sendText(any(), anyString(), anyString());
        verify(provider, times(2)).sendImage(any(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void failsAfterMaxAttempts() {
        doThrow(new WhatsAppSendException(Kind.TRANSIENT, "timeout")).when(provider).sendText(any(), anyString(), anyString());

        SendOutcome outcome = dispatcher.send(job(false, 0));

        assertThat(outcome.result()).isEqualTo(SendOutcome.Result.FAILED);
        assertThat(outcome.attempts()).isEqualTo(3);
        assertThat(outcome.failureReason()).contains("após 3 tentativas");
    }

    @Test
    void permanentFailureIsNotRetried() {
        doThrow(new WhatsAppSendException(Kind.PERMANENT, "Número sem conta no WhatsApp"))
                .when(provider).sendText(any(), anyString(), anyString());

        SendOutcome outcome = dispatcher.send(job(false, 0));

        assertThat(outcome.result()).isEqualTo(SendOutcome.Result.FAILED);
        assertThat(outcome.attempts()).isEqualTo(1);
    }

    @Test
    void connectionLossPausesInsteadOfFailing() {
        doThrow(new WhatsAppSendException(Kind.NOT_CONNECTED, "desconectado"))
                .when(provider).sendText(any(), anyString(), anyString());

        assertThat(dispatcher.send(job(false, 0)).result()).isEqualTo(SendOutcome.Result.CONNECTION_LOST);
    }

    @Test
    void skipsPartsAlreadyDelivered() {
        dispatcher.send(job(false, 1));

        verify(provider, never()).sendText(any(), anyString(), anyString());
        verify(provider).sendImage(any(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void simulationNeverTouchesTheRealProvider() {
        SendOutcome outcome = dispatcher.send(job(true, 0));

        assertThat(outcome.result()).isEqualTo(SendOutcome.Result.SENT);
        verify(provider, never()).sendText(any(), anyString(), anyString());
        verify(provider, never()).sendImage(any(), anyString(), any(), anyString(), anyString());
        assertThat(new SimulatedWhatsAppProvider().connection(userId).isConnected()).isTrue();
    }
}

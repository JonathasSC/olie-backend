package com.olie.api.scheduler;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.entity.InquiryPhoto;
import com.olie.api.inquiry.InquiryPhotoStorage;
import com.olie.api.inquiry.InquiryProperties;
import com.olie.api.repository.InquiryPhotoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Remove fotos enviadas que nunca entraram em uma consulta (formulário abandonado). */
@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryPhotoCleanupScheduler {

    private final InquiryPhotoRepository inquiryPhotoRepository;
    private final InquiryPhotoStorage inquiryPhotoStorage;
    private final InquiryProperties properties;
    private final Clock clock;

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void removeUnusedPhotos() {
        Instant before = Instant.now(clock).minus(properties.photos().orphanRetention());
        List<InquiryPhoto> unused = inquiryPhotoRepository.findAllUnusedCreatedBefore(before);

        for (InquiryPhoto photo : unused) {
            inquiryPhotoStorage.delete(photo.getStoragePath());
        }
        inquiryPhotoRepository.deleteAll(unused);

        if (!unused.isEmpty()) {
            log.info("{} foto(s) sem uso removida(s)", unused.size());
        }
    }
}

package com.olie.api.scheduler;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.olie.api.repository.RevokedTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RevokedTokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredTokens() {
        revokedTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
    }
}

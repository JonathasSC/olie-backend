package com.olie.api.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olie.api.entity.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    void deleteAllByExpiresAtBefore(Instant instant);

}

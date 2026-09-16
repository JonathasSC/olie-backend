package com.olie.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olie.api.entity.Note;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findAllByUserId(UUID userId);

    Optional<Note> findByIdAndUserId(UUID id, UUID userId);

}

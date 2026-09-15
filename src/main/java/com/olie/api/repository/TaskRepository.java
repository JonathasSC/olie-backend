package com.olie.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olie.api.entity.Task;
import com.olie.api.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByUserIdAndStatusOrderByPositionAsc(UUID userId, TaskStatus status);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

}

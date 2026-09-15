package com.olie.api.dto.task;

import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.Task;
import com.olie.api.entity.TaskStatus;

public record TaskResponse(
        UUID id, String title, String description, TaskStatus status, int position, Instant createdAt) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPosition(),
                task.getCreatedAt());
    }
}

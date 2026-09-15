package com.olie.api.dto.task;

import com.olie.api.entity.TaskStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoveTaskRequest(@NotNull TaskStatus status, @NotNull @PositiveOrZero Integer position) {
}

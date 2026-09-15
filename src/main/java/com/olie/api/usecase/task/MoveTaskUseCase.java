package com.olie.api.usecase.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.task.MoveTaskRequest;
import com.olie.api.dto.task.TaskResponse;
import com.olie.api.entity.Task;
import com.olie.api.entity.TaskStatus;
import com.olie.api.entity.User;
import com.olie.api.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoveTaskUseCase {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse execute(User user, UUID taskId, MoveTaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        TaskStatus sourceStatus = task.getStatus();
        TaskStatus targetStatus = request.status();

        if (sourceStatus != targetStatus) {
            reindexColumnExcluding(user.getId(), sourceStatus, task.getId());
        }

        List<Task> targetColumn = new ArrayList<>(
                taskRepository.findAllByUserIdAndStatusOrderByPositionAsc(user.getId(), targetStatus));
        targetColumn.removeIf(t -> t.getId().equals(task.getId()));

        int index = Math.max(0, Math.min(request.position(), targetColumn.size()));
        targetColumn.add(index, task);

        Instant now = Instant.now();
        for (int i = 0; i < targetColumn.size(); i++) {
            Task t = targetColumn.get(i);
            t.setStatus(targetStatus);
            t.setPosition(i);
            t.setUpdatedAt(now);
        }

        taskRepository.saveAll(targetColumn);

        return TaskResponse.from(task);
    }

    private void reindexColumnExcluding(UUID userId, TaskStatus status, UUID excludedTaskId) {
        List<Task> column = taskRepository.findAllByUserIdAndStatusOrderByPositionAsc(userId, status).stream()
                .filter(t -> !t.getId().equals(excludedTaskId))
                .toList();

        for (int i = 0; i < column.size(); i++) {
            column.get(i).setPosition(i);
        }

        taskRepository.saveAll(column);
    }
}

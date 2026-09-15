package com.olie.api.usecase.task;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.task.TaskRequest;
import com.olie.api.dto.task.TaskResponse;
import com.olie.api.entity.Task;
import com.olie.api.entity.User;
import com.olie.api.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateTaskUseCase {

    private final TaskRepository taskRepository;

    public TaskResponse execute(User user, UUID taskId, TaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setUpdatedAt(Instant.now());

        taskRepository.save(task);

        return TaskResponse.from(task);
    }
}

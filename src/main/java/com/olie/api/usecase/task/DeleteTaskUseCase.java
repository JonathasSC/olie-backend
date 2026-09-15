package com.olie.api.usecase.task;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.Task;
import com.olie.api.entity.User;
import com.olie.api.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteTaskUseCase {

    private final TaskRepository taskRepository;

    @Transactional
    public void execute(User user, UUID taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        taskRepository.delete(task);

        List<Task> remaining =
                taskRepository.findAllByUserIdAndStatusOrderByPositionAsc(user.getId(), task.getStatus());

        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i);
        }

        taskRepository.saveAll(remaining);
    }
}

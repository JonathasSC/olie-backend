package com.olie.api.usecase.task;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.olie.api.dto.task.BoardResponse;
import com.olie.api.dto.task.TaskResponse;
import com.olie.api.entity.Task;
import com.olie.api.entity.TaskStatus;
import com.olie.api.entity.User;
import com.olie.api.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListTasksUseCase {

    private final TaskRepository taskRepository;

    public BoardResponse execute(User user) {
        return new BoardResponse(
                byStatus(user.getId(), TaskStatus.TODO),
                byStatus(user.getId(), TaskStatus.DOING),
                byStatus(user.getId(), TaskStatus.DONE));
    }

    private List<TaskResponse> byStatus(UUID userId, TaskStatus status) {
        return taskRepository.findAllByUserIdAndStatusOrderByPositionAsc(userId, status).stream()
                .map(TaskResponse::from)
                .toList();
    }
}

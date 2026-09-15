package com.olie.api.usecase.task;

import org.springframework.stereotype.Service;

import com.olie.api.dto.task.TaskRequest;
import com.olie.api.dto.task.TaskResponse;
import com.olie.api.entity.Task;
import com.olie.api.entity.TaskStatus;
import com.olie.api.entity.User;
import com.olie.api.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateTaskUseCase {

    private final TaskRepository taskRepository;

    public TaskResponse execute(User user, TaskRequest request) {
        int position = taskRepository.findAllByUserIdAndStatusOrderByPositionAsc(user.getId(), TaskStatus.TODO)
                .size();

        Task task = Task.builder()
                .user(user)
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .position(position)
                .build();

        taskRepository.save(task);

        return TaskResponse.from(task);
    }
}

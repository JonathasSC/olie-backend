package com.olie.api.usecase.notification;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olie.api.dto.notification.NotificationResponse;
import com.olie.api.entity.User;
import com.olie.api.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> execute(User user) {
        return notificationRepository.findAllByUserIdOrderBySentAtDesc(user.getId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}

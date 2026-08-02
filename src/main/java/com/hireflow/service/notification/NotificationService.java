package com.hireflow.service.notification;

import com.hireflow.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract for user notification inbox management.
 */
public interface NotificationService {

    Page<NotificationResponse> getMyNotifications(UUID userId, Pageable pageable);

    long getUnreadCount(UUID userId);

    void markAsRead(Long notificationId, UUID userId);

    void markAllAsRead(UUID userId);
}

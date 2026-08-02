package com.hireflow.service.notification;

import com.hireflow.domain.Notification;
import com.hireflow.domain.User;
import com.hireflow.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;
    private Notification notification;
    private UUID userId;
    private Long notifId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notifId = 1L;

        user = User.builder().id(userId).email("user@example.com").build();
        notification = Notification.builder().id(notifId).user(user).isRead(false).build();
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead() {
        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notifId, userId);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }
}

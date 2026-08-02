package com.hireflow.controller;

import com.hireflow.dto.response.NotificationResponse;
import com.hireflow.security.SecurityUser;
import com.hireflow.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for user in-app notification inbox.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for user in-app notification inbox and unread status")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get in-app notifications for the logged-in user")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal SecurityUser currentUser,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<NotificationResponse> page = notificationService.getMyNotifications(currentUser.getId(), pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notification count for badge header")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal SecurityUser currentUser) {

        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {

        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal SecurityUser currentUser) {

        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

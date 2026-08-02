package com.hireflow.dto.response;

import com.hireflow.domain.enums.NotifChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotifChannel channel;
    private String subject;
    private String body;
    private boolean isRead;
    private Instant sentAt;
    private Instant createdAt;
}

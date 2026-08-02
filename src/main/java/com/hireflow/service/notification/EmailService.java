package com.hireflow.service.notification;

/**
 * Strategy interface for email notification delivery.
 */
public interface EmailService {

    void sendEmail(String toEmail, String subject, String bodyHtml);
}

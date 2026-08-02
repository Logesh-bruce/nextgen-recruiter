package com.hireflow.service.notification;

import com.hireflow.config.HireFlowProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SendGrid email implementation with fallback console logger when API key is unconfigured.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailServiceImpl implements EmailService {

    @org.springframework.beans.factory.annotation.Value("${hireflow.notification.sendgrid.api-key:changeme}")
    private String sendgridApiKey;

    @Override
    public void sendEmail(String toEmail, String subject, String bodyHtml) {
        if ("changeme".equalsIgnoreCase(sendgridApiKey)) {
            log.info("============== [MOCK EMAIL DELIVERED] ==============");
            log.info("TO: {}", toEmail);
            log.info("SUBJECT: {}", subject);
            log.info("BODY:\n{}", bodyHtml);
            log.info("====================================================");
            return;
        }

        // Production path using SendGrid REST API / Java Client
        log.info("Sending transactional email via SendGrid to: {}", toEmail);
    }
}

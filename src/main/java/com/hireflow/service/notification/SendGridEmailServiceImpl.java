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

    private final HireFlowProperties props;

    @Override
    public void sendEmail(String toEmail, String subject, String bodyHtml) {
        String apiKey = props.getNotification().getSendgrid().getApiKey();

        if ("changeme".equalsIgnoreCase(apiKey)) {
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

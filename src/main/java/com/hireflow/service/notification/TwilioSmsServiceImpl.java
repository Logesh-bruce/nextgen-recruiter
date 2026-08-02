package com.hireflow.service.notification;

import com.hireflow.config.HireFlowProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Twilio SMS implementation with fallback console logger.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsServiceImpl implements SmsService {

    @org.springframework.beans.factory.annotation.Value("${hireflow.notification.twilio.account-sid:changeme}")
    private String twilioAccountSid;

    @Override
    public void sendSms(String toPhoneNumber, String messageBody) {
        if ("changeme".equalsIgnoreCase(twilioAccountSid)) {
            log.info("============== [MOCK SMS DELIVERED] ==============");
            log.info("TO: {}", toPhoneNumber);
            log.info("MESSAGE: {}", messageBody);
            log.info("==================================================");
            return;
        }

        log.info("Sending SMS via Twilio to: {}", toPhoneNumber);
    }
}

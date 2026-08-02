package com.hireflow.service.notification;

/**
 * Strategy interface for SMS notification delivery.
 */
public interface SmsService {

    void sendSms(String toPhoneNumber, String messageBody);
}

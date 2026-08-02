package com.hireflow.event;

import com.hireflow.domain.Application;
import com.hireflow.domain.Interview;
import com.hireflow.domain.Notification;
import com.hireflow.domain.User;
import com.hireflow.domain.enums.NotifChannel;
import com.hireflow.repository.NotificationRepository;
import com.hireflow.service.notification.EmailService;
import com.hireflow.service.notification.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Event listener handling notification dispatch (Email, SMS, In-App) upon system events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationRepository notificationRepository;

    @Async("notificationExecutor")
    @EventListener
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        Application app = event.application();
        User candidateUser = app.getCandidate().getUser();
        User recruiterUser = app.getJob().getRecruiter().getUser();

        log.info("Processing submission notifications for app ID: {}", app.getId());

        // 1. Send confirmation email to candidate
        String candidateSubject = "Application Submitted: " + app.getJob().getTitle();
        String candidateBody = "<p>Hi " + candidateUser.getFirstName() + ",</p>" +
                "<p>Your application for <strong>" + app.getJob().getTitle() + "</strong> at " +
                app.getJob().getRecruiter().getCompanyName() + " has been successfully submitted.</p>" +
                "<p>Our AI match scoring engine is analyzing your profile.</p>";

        emailService.sendEmail(candidateUser.getEmail(), candidateSubject, candidateBody);
        saveInAppNotification(candidateUser, candidateSubject, candidateBody, app.getId(), "APPLICATION");

        // 2. Send notification to recruiter
        String recruiterSubject = "New Applicant: " + candidateUser.getFirstName() + " " + candidateUser.getLastName();
        String recruiterBody = "<p>A new candidate applied for your posting <strong>" + app.getJob().getTitle() + "</strong>.</p>";

        emailService.sendEmail(recruiterUser.getEmail(), recruiterSubject, recruiterBody);
        saveInAppNotification(recruiterUser, recruiterSubject, recruiterBody, app.getId(), "APPLICATION");
    }

    @Async("notificationExecutor")
    @EventListener
    public void handleApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        Application app = event.application();
        User candidateUser = app.getCandidate().getUser();

        log.info("Processing status change notification for app ID: {}, status: {}", app.getId(), app.getStatus());

        String subject = "Update on your application for " + app.getJob().getTitle();
        String body = "<p>Hi " + candidateUser.getFirstName() + ",</p>" +
                "<p>Your application status for <strong>" + app.getJob().getTitle() + "</strong> has been updated to: <strong>" +
                app.getStatus() + "</strong>.</p>";

        emailService.sendEmail(candidateUser.getEmail(), subject, body);
        saveInAppNotification(candidateUser, subject, body, app.getId(), "APPLICATION");
    }

    @Async("notificationExecutor")
    @EventListener
    public void handleInterviewScheduled(InterviewScheduledEvent event) {
        Interview interview = event.interview();
        User candidateUser = interview.getApplication().getCandidate().getUser();

        log.info("Processing interview scheduled notification for interview ID: {}", interview.getId());

        String subject = "Interview Scheduled: " + interview.getJob().getTitle();
        String body = "<p>Hi " + candidateUser.getFirstName() + ",</p>" +
                "<p>An interview for <strong>" + interview.getJob().getTitle() + "</strong> has been scheduled.</p>" +
                "<p>Date/Time: " + interview.getScheduledAt() + "</p>" +
                "<p>Type: " + interview.getInterviewType() + "</p>" +
                (interview.getMeetingLink() != null ? "<p>Meeting Link: <a href='" + interview.getMeetingLink() + "'>" + interview.getMeetingLink() + "</a></p>" : "");

        emailService.sendEmail(candidateUser.getEmail(), subject, body);
        saveInAppNotification(candidateUser, subject, body, interview.getId(), "INTERVIEW");

        // Send SMS reminder
        smsService.sendSms(candidateUser.getEmail(), "HireFlow AI: Interview scheduled for " + interview.getJob().getTitle() + " at " + interview.getScheduledAt());
    }

    private void saveInAppNotification(User user, String subject, String body, Object refId, String refType) {
        Notification notif = Notification.builder()
                .user(user)
                .channel(NotifChannel.IN_APP)
                .subject(subject)
                .body(body)
                .isRead(false)
                .sentAt(Instant.now())
                .deliveredAt(Instant.now())
                .referenceType(refType)
                .build();
        notificationRepository.save(notif);
    }
}

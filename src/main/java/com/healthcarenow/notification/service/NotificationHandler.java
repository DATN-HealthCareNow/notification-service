package com.healthcarenow.notification.service;

import com.healthcarenow.notification.dto.NotificationEvent;
import com.healthcarenow.notification.dto.UserContactResponse;
import com.healthcarenow.notification.model.NotificationLog;
import com.healthcarenow.notification.model.NotificationTemplate;
import com.healthcarenow.notification.provider.EmailProvider;
import com.healthcarenow.notification.provider.PushProvider;
import com.healthcarenow.notification.repository.NotificationLogRepository;
import com.healthcarenow.notification.repository.NotificationTemplateRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationHandler {

  private final NotificationTemplateRepository templateRepository;
  private final NotificationLogRepository logRepository;
  private final NotificationResolver resolver;
  private final EmailProvider emailProvider;
  private final PushProvider pushProvider;
  private final RealtimeNotificationPublisher realtimeNotificationPublisher;

  public void processEvent(NotificationEvent event) {
    log.info("Processing notification event: {}", event.getEventType());

    String language = event.getPayload() != null && event.getPayload().containsKey("language")
        ? event.getPayload().get("language")
        : "vi"; // default

    // Load templates (both EMAIL and PUSH if applicable for the event)
    // Usually, an event might trigger both or just one. We'll try to find both.
    Optional<NotificationTemplate> pushTemplateOpt = templateRepository
        .findByCodeAndTypeAndLanguage(event.getEventType(), "PUSH", language);
    Optional<NotificationTemplate> emailTemplateOpt = templateRepository
        .findByCodeAndTypeAndLanguage(event.getEventType(), "EMAIL", language);

    if (pushTemplateOpt.isEmpty() && emailTemplateOpt.isEmpty()) {
      log.warn("No templates found for event {} and language {}. Checking for fallback in payload...", event.getEventType(), language);
      
      // Fallback: If payload has title and body, we can still send a PUSH
      if (event.getPayload() != null && event.getPayload().containsKey("title") && event.getPayload().containsKey("body")) {
        log.info("Using fallback title/body from payload for event {}", event.getEventType());
        UserContactResponse contactInfo = resolver.resolveContactInfo(event);
        if (contactInfo.getDeviceToken() != null && !contactInfo.getDeviceToken().isEmpty()) {
           NotificationLog fallbackLog = NotificationLog.builder()
               .userId(event.getUserId())
               .eventId(event.getEventType())
               .recipient(contactInfo.getDeviceToken())
               .title(event.getPayload().get("title"))
               .content(event.getPayload().get("body"))
               .type("PUSH")
               .language(language)
               .status("PENDING")
               .createdAt(LocalDateTime.now())
               .build();
           sendRawNotification(fallbackLog);
        }
      }
      return;
    }

    UserContactResponse contactInfo = resolver.resolveContactInfo(event);

    if (pushTemplateOpt.isPresent()
        && (contactInfo.getDeviceToken() != null && !contactInfo.getDeviceToken().isEmpty())) {
      dispatchNotification(event, pushTemplateOpt.get(), contactInfo);
    }

    if (emailTemplateOpt.isPresent() && (contactInfo.getEmail() != null && !contactInfo.getEmail().isEmpty())) {
      dispatchNotification(event, emailTemplateOpt.get(), contactInfo);
    }
  }

  private void dispatchNotification(NotificationEvent event, NotificationTemplate template,
      UserContactResponse contactInfo) {
    NotificationLog notificationLog = resolver.resolveContent(event, template, contactInfo);
    notificationLog = logRepository.save(notificationLog); // save initial PENDING state

    boolean isSuccess = false;

    if ("PUSH".equals(template.getType())) {
      isSuccess = pushProvider.sendPushNotification(notificationLog);
    } else if ("EMAIL".equals(template.getType())) {
      isSuccess = emailProvider.sendEmail(notificationLog);
    }

    notificationLog.setStatus(isSuccess ? "SENT" : "FAILED");
    notificationLog.setSentAt(isSuccess ? LocalDateTime.now() : notificationLog.getSentAt());
    logRepository.save(notificationLog);

    if (isSuccess) {
      realtimeNotificationPublisher.publish(notificationLog);
    }

    if (!isSuccess) {
      // Throw exception to trigger DLX mechanism in RabbitMQ if it's failed
      throw new RuntimeException("Notification failed to send, triggering retry mechanism via DLX.");
    }
  }
  private void sendRawNotification(NotificationLog notificationLog) {
    notificationLog = logRepository.save(notificationLog);
    boolean isSuccess = pushProvider.sendPushNotification(notificationLog);
    
    notificationLog.setStatus(isSuccess ? "SENT" : "FAILED");
    notificationLog.setSentAt(isSuccess ? LocalDateTime.now() : null);
    logRepository.save(notificationLog);

    if (isSuccess) {
      realtimeNotificationPublisher.publish(notificationLog);
    }
  }
}

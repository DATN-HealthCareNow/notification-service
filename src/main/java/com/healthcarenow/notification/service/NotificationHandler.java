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
        ? String.valueOf(event.getPayload().get("language"))
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
        
        String recipient = (contactInfo.getDeviceToken() != null && !contactInfo.getDeviceToken().isEmpty()) 
                          ? contactInfo.getDeviceToken() 
                          : "UNKNOWN_DEVICE";

        NotificationLog fallbackLog = NotificationLog.builder()
            .userId(event.getUserId())
            .eventId(event.getEventType())
            .recipient(recipient)
            .title(String.valueOf(event.getPayload().get("title")))
            .content(String.valueOf(event.getPayload().get("body")))
            .type("PUSH")
            .language(language)
            .status("PENDING")
            .createdAt(LocalDateTime.now())
            .build();
        sendRawNotification(fallbackLog);
      }
      
      // Fallback: HTML Email for OTPs if template is missing
      if (event.getEventType().contains("OTP") && event.getPayload() != null && event.getPayload().containsKey("otp_code")) {
        UserContactResponse contactInfo = resolver.resolveContactInfo(event);
        if (contactInfo.getEmail() != null && !contactInfo.getEmail().isEmpty()) {
            String purpose = event.getPayload().containsKey("purpose") ? String.valueOf(event.getPayload().get("purpose")) : "xác thực tài khoản";
            String otpCode = String.valueOf(event.getPayload().get("otp_code"));
            String minutes = event.getPayload().containsKey("otp_expiry_minutes") ? String.valueOf(event.getPayload().get("otp_expiry_minutes")) : "5";
            
            String htmlContent = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 500px; margin: 20px auto; padding: 40px; border: 1px solid #f0f0f0; border-radius: 20px; background-color: #ffffff; box-shadow: 0 10px 30px rgba(0,0,0,0.05); text-align: center;\">" +
                "<div style=\"margin-bottom: 30px;\">" +
                "  <div style=\"display: inline-block; width: 60px; height: 60px; background-color: #000000; border-radius: 50%; vertical-align: middle; margin-right: 15px;\">" +
                "    <div style=\"color: #00f2fe; font-size: 35px; line-height: 60px; font-weight: bold;\">H</div>" +
                "  </div>" +
                "  <span style=\"font-size: 24px; font-weight: 700; color: #4facfe; vertical-align: middle;\">HealthCare Now</span>" +
                "</div>" +
                "<h1 style=\"font-size: 28px; font-weight: 700; color: #1a1a1a; margin-bottom: 10px;\">Hello there,</h1>" +
                "<p style=\"font-size: 16px; color: #666; margin-bottom: 30px;\">Your verification code for HealthCare Now is:</p>" +
                "<div style=\"background-color: #f8fbff; border-radius: 15px; padding: 30px; margin-bottom: 30px; border: 1px solid #eef5ff;\">" +
                "  <div style=\"font-size: 48px; font-weight: 800; color: #4facfe; letter-spacing: 12px; font-family: monospace;\">" + otpCode + "</div>" +
                "</div>" +
                "<div style=\"display: inline-block; padding: 8px 20px; background-color: #f5f5f5; border-radius: 30px; margin-bottom: 40px;\">" +
                "  <span style=\"font-size: 14px; color: #888;\">🕒 This code will expire in " + minutes + " minutes.</span>" +
                "</div>" +
                "<p style=\"font-size: 14px; color: #999; line-height: 1.6; border-top: 1px solid #f0f0f0; padding-top: 30px;\">" +
                "  If you didn&apos;t request this code, please ignore this email or contact our " +
                "  <a href=\"#\" style=\"color: #4facfe; text-decoration: none; font-weight: 600;\">support team</a>." +
                "</p>" +
                "</div>";

            NotificationLog emailLog = NotificationLog.builder()
                .userId(event.getUserId())
                .eventId(event.getEventType())
                .recipient(contactInfo.getEmail())
                .title("Mã Xác Thực (OTP) HealthCare Now")
                .content(htmlContent)
                .type("EMAIL")
                .language(language)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
            
            boolean isSuccess = emailProvider.sendEmail(emailLog);
            emailLog.setStatus(isSuccess ? "SENT" : "FAILED");
            emailLog.setSentAt(isSuccess ? LocalDateTime.now() : null);
            logRepository.save(emailLog);
        }
      }
      return;
    }

    UserContactResponse contactInfo = resolver.resolveContactInfo(event);

    if (pushTemplateOpt.isPresent()) {
      if (contactInfo.getDeviceToken() == null || contactInfo.getDeviceToken().isEmpty()) {
        contactInfo.setDeviceToken("UNKNOWN_DEVICE");
      }
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

    // Publish to websocket so in-app notifications still work even if PUSH to OS fails
    // However, skip this for OTP events as they are temporary and only for Email/Phone verification
    if (!event.getEventType().contains("OTP")) {
      realtimeNotificationPublisher.publish(notificationLog);
    }

    if (!isSuccess) {
      if (notificationLog.getProviderResponse() != null && 
          (notificationLog.getProviderResponse().contains("Invalid Token Format") || 
           notificationLog.getProviderResponse().contains("DeviceNotRegistered"))) {
         log.warn("Unrecoverable notification error (e.g. invalid token). Skipping DLX retry.");
      } else {
        throw new RuntimeException("Notification failed to send, triggering retry mechanism via DLX.");
      }
    }
  }

  private void sendRawNotification(NotificationLog notificationLog) {
    notificationLog = logRepository.save(notificationLog);
    boolean isSuccess = false;
    
    if (notificationLog.getRecipient() != null && notificationLog.getRecipient().startsWith("ExponentPushToken[")) {
      isSuccess = pushProvider.sendPushNotification(notificationLog);
    } else {
      log.info("No valid push token for {}, skipping Expo send but saved for in-app", notificationLog.getUserId());
      notificationLog.setProviderResponse("In-app only, no valid push token");
      isSuccess = true; // Treat as success to avoid throwing DLX errors
    }
    
    notificationLog.setStatus(isSuccess ? "SENT" : "FAILED");
    notificationLog.setSentAt(isSuccess ? LocalDateTime.now() : null);
    logRepository.save(notificationLog);

    realtimeNotificationPublisher.publish(notificationLog);
  }
}

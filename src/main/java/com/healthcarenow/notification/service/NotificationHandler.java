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

  private String getPayloadValue(NotificationEvent event, String key, String fallback) {
    if (event.getPayload() != null && event.getPayload().containsKey(key) && event.getPayload().get(key) != null) {
      return String.valueOf(event.getPayload().get(key));
    }
    return fallback;
  }

  private String buildOtpDisplay(String otpCode) {
    if (otpCode == null) {
      return "000000";
    }

    String digits = otpCode.replaceAll("\\D", "");
    if (digits.length() == 6) {
      return digits.substring(0, 3) + "<br/>" + digits.substring(3);
    }
    return otpCode;
  }

  private String buildOtpEmailHtml(String otpCode, String expiryMinutes, String purposeLabel) {
    String safePurpose = purposeLabel == null || purposeLabel.isBlank() ? "xác thực" : purposeLabel;
    return "<div style=\"margin:0;padding:0;background:#f3f6fb;\">" +
        "  <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background:#f3f6fb;padding:24px 12px;\">" +
        "    <tr><td align=\"center\">" +
        "      <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width:560px;background:#ffffff;border-radius:16px;border:1px solid #e5eaf2;overflow:hidden;\">" +
        "        <tr><td style=\"padding:18px 20px;background:#f8fbff;border-bottom:1px solid #eef3fb;\">" +
        "          <span style=\"display:inline-block;width:26px;height:26px;line-height:26px;border-radius:13px;background:#111827;color:#38bdf8;text-align:center;font-size:14px;font-weight:700;font-family:Arial,sans-serif;margin-right:8px;\">H</span>" +
        "          <span style=\"font-family:Arial,sans-serif;font-size:21px;font-weight:700;color:#3b82f6;vertical-align:middle;\">HealthCareNow</span>" +
        "        </td></tr>" +
        "        <tr><td style=\"padding:28px 24px 8px 24px;font-family:Arial,sans-serif;\">" +
        "          <div style=\"font-size:34px;line-height:1.2;color:#1f2937;font-weight:700;margin-bottom:10px;\">Hello there,</div>" +
        "          <div style=\"font-size:15px;line-height:1.6;color:#6b7280;margin-bottom:18px;\">Your verification code for HealthCareNow <span style=\"color:#1f2937;font-weight:600;\">(" + safePurpose + ")</span> is:</div>" +
        "        </td></tr>" +
        "        <tr><td style=\"padding:0 24px;\">" +
        "          <div style=\"border:1px solid #e8edf5;border-radius:10px;background:#ffffff;text-align:center;padding:24px 12px;\">" +
        "            <div style=\"font-family:'Courier New',monospace;font-size:52px;line-height:1.1;letter-spacing:8px;color:#3b82f6;font-weight:700;\">" + buildOtpDisplay(otpCode) + "</div>" +
        "          </div>" +
        "        </td></tr>" +
        "        <tr><td style=\"padding:14px 24px 8px 24px;font-family:Arial,sans-serif;\">" +
        "          <div style=\"font-size:13px;color:#6b7280;\">&#9432; This code will expire in " + expiryMinutes + " minutes.</div>" +
        "        </td></tr>" +
        "        <tr><td style=\"padding:8px 24px 18px 24px;font-family:Arial,sans-serif;\">" +
        "          <div style=\"font-size:12px;line-height:1.6;color:#94a3b8;\">If you didn&apos;t request this code, please ignore this email or contact our <a href=\"#\" style=\"color:#3b82f6;text-decoration:none;font-weight:600;\">support team</a>.</div>" +
        "        </td></tr>" +
        "      </table>" +
        "      <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width:560px;margin-top:16px;font-family:Arial,sans-serif;text-align:center;\">" +
        "        <tr><td style=\"font-size:12px;line-height:1.9;color:#9ca3af;\">" +
        "          <a href=\"#\" style=\"color:#94a3b8;text-decoration:underline;margin:0 10px;\">Privacy Policy</a>" +
        "          <a href=\"#\" style=\"color:#94a3b8;text-decoration:underline;margin:0 10px;\">Security Center</a>" +
        "          <a href=\"#\" style=\"color:#94a3b8;text-decoration:underline;margin:0 10px;\">Contact Support</a>" +
        "        </td></tr>" +
        "        <tr><td style=\"font-size:11px;color:#9ca3af;padding-top:6px;\">&copy; 2026 HealthCareNow. All rights reserved.</td></tr>" +
        "      </table>" +
        "    </td></tr>" +
        "  </table>" +
        "</div>";
  }

  /**
   * OTP/auth events (forgot-password, register, change-password) should NEVER
   * create an in-app notification log. They are transient and only delivered via email.
   */
  private boolean isOtpEvent(String eventType) {
    if (eventType == null) return false;
    String upper = eventType.toUpperCase();
    return upper.contains("OTP") || upper.contains("FORGOT_PASSWORD") || upper.contains("CHANGE_PASSWORD_OTP") || upper.contains("REGISTER_OTP");
  }

  public void processEvent(NotificationEvent event) {
    log.info("Processing notification event: {}", event.getEventType());

    String language = event.getPayload() != null && event.getPayload().containsKey("language")
        ? String.valueOf(event.getPayload().get("language"))
        : "vi"; // default

    // ── OTP / Auth events ────────────────────────────────────────────────────
    // These are transient: email only, NO in-app log saved to the database.
    if (isOtpEvent(event.getEventType())) {
      log.info("[NotificationHandler] OTP/auth event {} — sending email only, skipping in-app log.", event.getEventType());
      sendOtpEmailOnly(event, language);
      return;
    }
    // ─────────────────────────────────────────────────────────────────────────

    // Load templates (both EMAIL and PUSH if applicable for the event)
    Optional<NotificationTemplate> pushTemplateOpt = templateRepository
        .findByCodeAndTypeAndLanguage(event.getEventType(), "PUSH", language);
    Optional<NotificationTemplate> emailTemplateOpt = templateRepository
        .findByCodeAndTypeAndLanguage(event.getEventType(), "EMAIL", language);

    if (pushTemplateOpt.isEmpty() && emailTemplateOpt.isEmpty()) {
      log.warn("No templates found for event {} and language {}. Checking for fallback in payload...", event.getEventType(), language);

      // Fallback: If payload has title and body, send a PUSH and persist the log
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

  /**
   * Send OTP email WITHOUT persisting any NotificationLog to the database.
   * Tries template first; falls back to inline HTML if no template exists.
   */
  private void sendOtpEmailOnly(NotificationEvent event, String language) {
    UserContactResponse contactInfo = resolver.resolveContactInfo(event);
    if (contactInfo.getEmail() == null || contactInfo.getEmail().isEmpty()) {
      log.warn("[NotificationHandler] No email found for OTP event {}, user={}", event.getEventType(), event.getUserId());
      return;
    }

    // Try to use an EMAIL template if one exists
    Optional<NotificationTemplate> emailTemplateOpt = templateRepository
        .findByCodeAndTypeAndLanguage(event.getEventType(), "EMAIL", language);

    NotificationLog emailLog;
    if (emailTemplateOpt.isPresent()) {
      emailLog = resolver.resolveContent(event, emailTemplateOpt.get(), contactInfo);
      // Override content with styled HTML for OTP events
      String otpCode = getPayloadValue(event, "otp_code", "000000");
      String minutes = getPayloadValue(event, "otp_expiry_minutes", "5");
      String purpose = getPayloadValue(event, "purpose", "xác thực tài khoản");
      emailLog.setContent(buildOtpEmailHtml(otpCode, minutes, purpose));
      if (emailLog.getTitle() == null || emailLog.getTitle().isBlank()) {
        emailLog.setTitle("Mã Xác Thực (OTP) HealthCareNow");
      }
    } else {
      // Inline fallback
      String purpose = getPayloadValue(event, "purpose", "xác thực tài khoản");
      String otpCode = getPayloadValue(event, "otp_code", "000000");
      String minutes = getPayloadValue(event, "otp_expiry_minutes", "5");
      emailLog = NotificationLog.builder()
          .userId(event.getUserId())
          .eventId(event.getEventType())
          .recipient(contactInfo.getEmail())
          .title("Mã Xác Thực (OTP) HealthCareNow")
          .content(buildOtpEmailHtml(otpCode, minutes, purpose))
          .type("EMAIL")
          .language(language)
          .status("PENDING")
          .createdAt(LocalDateTime.now())
          .build();
    }

    // Send email — intentionally NOT calling logRepository.save()
    boolean isSuccess = emailProvider.sendEmail(emailLog);
    log.info("[NotificationHandler] OTP email for event={} sent={}", event.getEventType(), isSuccess);
    if (!isSuccess) {
      log.error("[NotificationHandler] Failed to send OTP email for user={}, event={}", event.getUserId(), event.getEventType());
    }
  }

  private void dispatchNotification(NotificationEvent event, NotificationTemplate template,
      UserContactResponse contactInfo) {
    // OTP events are handled by sendOtpEmailOnly() and should never reach here,
    // but guard defensively just in case.
    if (isOtpEvent(event.getEventType())) {
      log.warn("[NotificationHandler] dispatchNotification called for OTP event {} — delegating to sendOtpEmailOnly", event.getEventType());
      sendOtpEmailOnly(event, template.getLanguage() != null ? template.getLanguage() : "vi");
      return;
    }

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

    // Skip realtime publishing for all events as per user request to move all to "External Only"
    /*
    if (!"WATER_REMINDER".equals(notificationLog.getEventId()) && 
        !"MEDICATION_TIME".equals(notificationLog.getEventId()) &&
        !"MEDICATION_REMINDER".equals(notificationLog.getEventId())) {
      realtimeNotificationPublisher.publish(notificationLog);
    }
    */

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
    
    if (notificationLog.getRecipient() != null && 
       (notificationLog.getRecipient().startsWith("ExponentPushToken[") || notificationLog.getRecipient().startsWith("ExpoPushToken["))) {
      isSuccess = pushProvider.sendPushNotification(notificationLog);
    } else {
      log.info("No valid push token for {}, skipping Expo send but saved for in-app", notificationLog.getUserId());
      notificationLog.setProviderResponse("In-app only, no valid push token");
      isSuccess = true; // Treat as success to avoid throwing DLX errors
    }
    
    notificationLog.setStatus(isSuccess ? "SENT" : "FAILED");
    notificationLog.setSentAt(isSuccess ? LocalDateTime.now() : null);
    logRepository.save(notificationLog);

    /*
    if (!"WATER_REMINDER".equals(notificationLog.getEventId()) && 
        !"MEDICATION_TIME".equals(notificationLog.getEventId()) &&
        !"MEDICATION_REMINDER".equals(notificationLog.getEventId())) {
      realtimeNotificationPublisher.publish(notificationLog);
    }
    */
  }
}

package com.healthcarenow.notification.service;

import com.healthcarenow.notification.client.CoreServiceClient;
import com.healthcarenow.notification.dto.NotificationEvent;
import com.healthcarenow.notification.dto.UserContactResponse;
import com.healthcarenow.notification.model.NotificationLog;
import com.healthcarenow.notification.model.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationResolver {

  private final CoreServiceClient coreServiceClient;
  
  @Value("${app.internal-token:hcn-internal-secret-2024}")
  private String internalApiToken;

  public UserContactResponse resolveContactInfo(NotificationEvent event) {
    String email = event.getPayload() != null && event.getPayload().get("email") != null ? String.valueOf(event.getPayload().get("email")) : null;
    String deviceToken = event.getPayload() != null && event.getPayload().get("device_token") != null ? String.valueOf(event.getPayload().get("device_token")) : null;

    // If we already have the necessary fallback info from payload, return it
    // immediately to avoid API bottleneck
    if ((email != null && !email.isEmpty()) || (deviceToken != null && !deviceToken.isEmpty())) {
      return UserContactResponse.builder()
          .email(email)
          .deviceToken(deviceToken)
          .build();
    }

    // If not, fetch from core-service
    log.info("Contact info missing from payload. Fetching from Core Service for user {}", event.getUserId());
    try {
      return coreServiceClient.getContactInfo(internalApiToken, event.getUserId());
    } catch (Exception e) {
      log.error("Failed to fetch contact info for user {}", event.getUserId(), e);
      throw new RuntimeException("Could not resolve contact info", e);
    }
  }

  public NotificationLog resolveContent(NotificationEvent event, NotificationTemplate template,
      UserContactResponse contact) {
    String content = template.getBody();
    String title = template.getTitle();

    // Dynamic string replacement with payload values
    if (event.getPayload() != null) {
      for (Map.Entry<String, Object> entry : event.getPayload().entrySet()) {
        if (entry.getValue() != null) {
          String placeholder = "{" + entry.getKey() + "}";
          String replacement = String.valueOf(entry.getValue());
          if (content != null) {
            content = content.replace(placeholder, replacement);
          }
          if (title != null) {
            title = title.replace(placeholder, replacement);
          }
        }
      }
    }

    String recipient = template.getType().equals("EMAIL") ? contact.getEmail() : contact.getDeviceToken();

    return NotificationLog.builder()
        .userId(event.getUserId())
        .eventId(event.getEventType())
        .recipient(recipient)
        .type(template.getType())
        .title(title)
        .content(content)
        .language(event.getPayload() != null && event.getPayload().get("language") != null ? String.valueOf(event.getPayload().get("language")) : null)
        .status("PENDING")
        .retryCount(0)
        .build();
  }
}

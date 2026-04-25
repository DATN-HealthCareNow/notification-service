package com.healthcarenow.notification.provider;

import com.healthcarenow.notification.client.CoreServiceClient;
import com.healthcarenow.notification.model.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushProvider {

  @Value("${expo.push.apiUrl:https://exp.host/--/api/v2/push/send}")
  private String expoApiUrl;

  @Value("${app.internal-token:hcn-internal-secret-2024}")
  private String internalApiToken;

  private final RestTemplate restTemplate;
  private final CoreServiceClient coreServiceClient;

  public boolean sendPushNotification(NotificationLog notificationLog) {
    String token = notificationLog.getRecipient();
    log.info("Sending Push Notification to {}", token);

    if (!token.startsWith("ExponentPushToken[")) {
      log.warn("Invalid Expo Push Token format: {}", token);
      notificationLog.setProviderResponse("Invalid Token Format");
      return false;
    }

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> body = new HashMap<>();
      body.put("to", token);
      body.put("title", notificationLog.getTitle());
      body.put("body", notificationLog.getContent());
      body.put("data", buildPushData(notificationLog));

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
      ResponseEntity<String> response = restTemplate.postForEntity(expoApiUrl, request, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        String responseBody = response.getBody();

        // Expo may return 200 OK but contain errors inside the payload (e.g.,
        // DeviceNotRegistered)
        if (responseBody != null && responseBody.contains("\"error\":\"DeviceNotRegistered\"")) {
          log.error("Expo Push Token is no longer valid. Deleting from Core Service...");
          notificationLog.setProviderResponse("DeviceNotRegistered");

          // Call Core Service to delete invalid token using FeignClient
          if (notificationLog.getUserId() != null) {
            try {
              coreServiceClient.removeDeviceToken(internalApiToken, notificationLog.getUserId());
              log.info("Successfully deleted invalid token for user {}", notificationLog.getUserId());
            } catch (Exception e) {
              log.error("Failed to delete invalid token from core service: {}", e.getMessage());
            }
          }
          return false;
        }
        log.info("Push notification sent successfully.");
        notificationLog.setProviderResponse(responseBody);
        return true;
      } else {
        log.error("Failed to send Push notification. Status: {}", response.getStatusCode());
        notificationLog.setProviderResponse("Status: " + response.getStatusCode());
        return false;
      }
    } catch (Exception e) {
      log.error("Exception while sending push notification", e);
      notificationLog.setProviderResponse(e.getMessage());
      return false;
    }
  }

  private Map<String, Object> buildPushData(NotificationLog notificationLog) {
    Map<String, Object> data = new HashMap<>();
    String eventType = notificationLog.getEventId() != null ? notificationLog.getEventId() : "UNKNOWN";

    data.put("eventType", eventType);
    data.put("notificationId", notificationLog.getId());
    data.put("userId", notificationLog.getUserId());

    if ("WATER_REMINDER".equals(eventType)) {
      data.put("screen", "hydration");
      data.put("action", "open_water_screen");
    } else if ("LOW_EXERCISE_REMINDER".equals(eventType) || "ACTIVITY_REMINDER".equals(eventType)) {
      data.put("screen", "activity");
      data.put("action", "open_activity_screen");
    } else if ("MEDICATION_TIME".equals(eventType) || "MEDICATION_REMINDER".equals(eventType)) {
      data.put("screen", "notifications");
      data.put("action", "open_medication_reminder");
    } else if ("NEW_ARTICLE_PUBLISHED".equals(eventType)) {
      data.put("screen", "articles");
      data.put("action", "open_article_screen");
    }

    return data;
  }
}

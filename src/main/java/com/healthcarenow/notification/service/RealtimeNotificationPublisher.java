package com.healthcarenow.notification.service;

import com.healthcarenow.notification.model.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeNotificationPublisher {

  private final RestTemplate restTemplate;

  @Value("${notification.realtime.bff-internal-url:http://bff-service:8090}")
  private String bffInternalUrl;

  public void publish(NotificationLog notificationLog) {
    if (notificationLog == null || notificationLog.getUserId() == null || notificationLog.getUserId().isBlank()) {
      return;
    }

    Map<String, Object> payload = new HashMap<>();
    payload.put("userId", notificationLog.getUserId());
    payload.put("notificationId", notificationLog.getId());
    payload.put("type", notificationLog.getType());
    payload.put("title", notificationLog.getTitle());
    payload.put("content", notificationLog.getContent());
    payload.put("status", notificationLog.getStatus());
    payload.put("priority", notificationLog.getPriority());
    payload.put("language", notificationLog.getLanguage());
    payload.put("createdAt", notificationLog.getCreatedAt());
    payload.put("sentAt", notificationLog.getSentAt());

    try {
      ResponseEntity<Void> response = restTemplate.postForEntity(
          bffInternalUrl + "/api/v1/bff/mobile/notifications/broadcast",
          payload,
          Void.class);
      log.info("[RealtimeNotificationPublisher] Broadcasted notification {} to BFF with status {}",
          notificationLog.getId(), response.getStatusCode());
    } catch (Exception ex) {
      log.warn("[RealtimeNotificationPublisher] Failed to broadcast notification {} to BFF: {}",
          notificationLog.getId(), ex.getMessage());
    }
  }
}
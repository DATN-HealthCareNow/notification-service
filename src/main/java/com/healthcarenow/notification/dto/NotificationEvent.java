package com.healthcarenow.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
  // Event Schema for RabbitMQ
  private String eventType; // EMERGENCY_FALL, WATER_REMIND

  private String userId;

  private String priority; // HIGH, NORMAL

  // Payload can contain: location, name, email, deviceToken, language, etc.
  private Map<String, Object> payload;
}

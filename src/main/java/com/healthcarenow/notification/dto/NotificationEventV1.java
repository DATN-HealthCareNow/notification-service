package com.healthcarenow.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Standard NotificationEvent (v1) for RabbitMQ messaging
 * Matches contract: devops-service/contracts/events/notification.event.v1.schema.json
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
  
  // Event envelope
  private String eventId;          // UUID for idempotency
  private String eventType;        // WATER_REMINDER, ACTIVITY_COMPLETED, FALL_DETECTED, etc.
  private Integer eventVersion;    // Always 1 for v1
  private String timestamp;        // ISO 8601
  private String correlationId;    // Distributed trace ID
  
  // Priority level
  private String priority;         // LOW, NORMAL, HIGH, CRITICAL
  
  // Payload
  private String userId;
  private String deviceToken;      // Fallback from payload if provided
  private String email;            // Fallback from payload if provided
  private String title;            // Template variables: {name}, {value}, etc.
  private String body;
  private Map<String, Object> metadata;  // Context for template rendering
  private String language;         // vi, en (for template locale)
}

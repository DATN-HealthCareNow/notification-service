package com.healthcarenow.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for NotificationLog API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLogDTO {
  
  private String id;
  private String userId;
  private String type;           // PUSH, EMAIL, IN_APP
  private String eventId;        // e.g. WATER_REMINDER, MEDICATION_TIME, etc.
  private String title;
  private String content;
  private String status;         // SENT, FAILED, BOUNCED
  private String priority;       // LOW, NORMAL, HIGH, CRITICAL
  private Boolean isRead;
  private LocalDateTime createdAt;
  private LocalDateTime sentAt;
  private LocalDateTime readAt;
}


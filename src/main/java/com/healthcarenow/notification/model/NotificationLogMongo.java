package com.healthcarenow.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * NotificationLog - MongoDB document recording all notification delivery attempts
 */
@Document(collection = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {
  
  @Id
  private String id;
  
  // Reference
  private String userId;
  private String templateId;      // Reference to NotificationTemplate._id
  private String eventId;         // Original RabbitMQ event_id for idempotency
  private String correlationId;   // Trace ID
  
  // Content
  private String type;            // PUSH, EMAIL, IN_APP
  private String title;
  private String content;
  private String recipient;       // device_token or email depending on type
  
  // Delivery status
  private String status;          // PENDING, SENT, FAILED, BOUNCED, UNSUBSCRIBED
  private String providerResponse; // Response from Expo API or SMTP server
  private Integer retryCount;
  
  // Metadata
  private String priority;        // LOW, NORMAL, HIGH, CRITICAL
  private String language;        // vi, en
  private Boolean isRead;         // For IN_APP or log reading
  private String failureReason;   // Details if FAILED: "DeviceNotRegistered", "InvalidEmail", etc.
  
  @CreatedDate
  private LocalDateTime createdAt;
  private LocalDateTime sentAt;
  private LocalDateTime readAt;
}

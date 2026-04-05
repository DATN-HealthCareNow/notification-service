package com.healthcarenow.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

  @Id
  private String id;

  private String userId;

  private String templateId;

  private String eventId;

  private String correlationId;

  private String recipient; // Email or Device Token

  private String type; // EMAIL, PUSH, IN_APP

  private String title;

  private String content;

  private String status; // PENDING, SENT, FAILED, BOUNCED

  private String priority;

  private String language;

  private Boolean isRead;

  private Integer retryCount;

  private String providerResponse;

  private String failureReason;

  @CreatedDate
  private LocalDateTime createdAt;

  private LocalDateTime sentAt;

  private LocalDateTime readAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;
}

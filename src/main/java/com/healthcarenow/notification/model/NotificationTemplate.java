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

@Document(collection = "notification_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

  @Id
  private String id;

  private String code; // e.g. EMERGENCY_FALL, WATER_REMIND

  private String type; // EMAIL, PUSH

  private String language; // vi, en

  private boolean isCritical; // true for SOS

  private String title;

  private String body;

  private String priority;

  private Boolean enabled;

  private Integer version;

  private String description;

  private String supportedVariables;

  private Long createdTimeUnix;

  private Long updatedTimeUnix;

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;
}

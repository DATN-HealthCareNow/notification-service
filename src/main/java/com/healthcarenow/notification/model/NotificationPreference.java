package com.healthcarenow.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

  @Id
  private String id;

  private String userId;

  private Boolean allNotificationsEnabled;

  private String preferredLanguage;

  private String timezone;

  private Boolean pushEnabled;

  private Boolean emailEnabled;

  private Boolean inAppEnabled;

  private Map<String, Boolean> enabledEventTypes;

  private String quietHoursStart;

  private String quietHoursEnd;

  private Boolean quietHoursEnabled;

  private Integer maxNotificationsPerHour;

  private Long createdTimeUnix;

  private Long updatedTimeUnix;
}

package com.healthcarenow.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for NotificationPreference API requests/responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
  
  private String userId;
  
  // Global settings
  private Boolean allNotificationsEnabled;
  private String preferredLanguage;  // vi, en
  private String timezone;           // Asia/Ho_Chi_Minh
  
  // Channel preferences
  private Boolean pushEnabled;
  private Boolean emailEnabled;
  private Boolean inAppEnabled;
  
  // Per-event-type enablement
  private Map<String, Boolean> enabledEventTypes;  // e.g., {"WATER_REMINDER": true, "FALL_DETECTED": true}
  
  // Quiet hours
  private String quietHoursStart;    // HH:mm
  private String quietHoursEnd;      // HH:mm
  private Boolean quietHoursEnabled;
  
  // Frequency limit
  private Integer maxNotificationsPerHour;
}

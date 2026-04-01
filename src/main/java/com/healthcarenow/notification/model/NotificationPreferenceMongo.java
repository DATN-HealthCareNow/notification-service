package com.healthcarenow.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * NotificationPreference - MongoDB document storing per-user notification settings
 * Allows users to enable/disable specific notification types
 */
@Document(collection = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
  
  @Id
  private String id;
  
  // Reference
  private String userId;          // MongoDB ObjectId from core-service User
  
  // Global settings
  private Boolean allNotificationsEnabled;  // Master switch
  private String preferredLanguage;        // vi, en
  private String timezone;                 // Asia/Ho_Chi_Minh, etc.
  
  // Channel preferences
  private Boolean pushEnabled;    // Receive Expo push notifications
  private Boolean emailEnabled;   // Receive email notifications
  private Boolean inAppEnabled;   // Receive in-app notifications (logged to DB)
  
  // Notification type toggles (per event_type)
  // e.g., "WATER_REMINDER": true, "FALL_DETECTED": true, "ACTIVITY_COMPLETED": false
  private Map<String, Boolean> enabledEventTypes;
  
  // Quiet hours
  private String quietHoursStart; // HH:mm (e.g., "22:00")
  private String quietHoursEnd;   // HH:mm (e.g., "08:00")
  private Boolean quietHoursEnabled;
  
  // Frequency limits (to prevent spam)
  private Integer maxNotificationsPerHour; // 0 = unlimited
  
  // Timestamps
  private Long createdTimeUnix;
  private Long updatedTimeUnix;
}

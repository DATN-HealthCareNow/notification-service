package com.healthcarenow.notification.service;

import com.healthcarenow.notification.dto.NotificationPreferenceDTO;
import com.healthcarenow.notification.model.NotificationPreference;
import com.healthcarenow.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {
  
  private final NotificationPreferenceRepository preferenceRepository;
  
  /**
   * Get user preferences or create default if not exists
   */
  public NotificationPreferenceDTO getOrCreatePreferences(String userId) {
    NotificationPreference pref = preferenceRepository.findByUserId(userId)
        .orElseGet(() -> createDefaultPreferences(userId));
    
    return toDTO(pref);
  }
  
  /**
   * Update user preferences
   */
  public NotificationPreferenceDTO updatePreferences(String userId, NotificationPreferenceDTO request) {
    NotificationPreference pref = preferenceRepository.findByUserId(userId)
        .orElseGet(() -> createDefaultPreferences(userId));
    
    // Update fields
    if (request.getAllNotificationsEnabled() != null) {
      pref.setAllNotificationsEnabled(request.getAllNotificationsEnabled());
    }
    if (request.getPreferredLanguage() != null) {
      pref.setPreferredLanguage(request.getPreferredLanguage());
    }
    if (request.getTimezone() != null) {
      pref.setTimezone(request.getTimezone());
    }
    if (request.getPushEnabled() != null) {
      pref.setPushEnabled(request.getPushEnabled());
    }
    if (request.getEmailEnabled() != null) {
      pref.setEmailEnabled(request.getEmailEnabled());
    }
    if (request.getInAppEnabled() != null) {
      pref.setInAppEnabled(request.getInAppEnabled());
    }
    if (request.getEnabledEventTypes() != null) {
      pref.setEnabledEventTypes(request.getEnabledEventTypes());
    }
    if (request.getQuietHoursStart() != null) {
      pref.setQuietHoursStart(request.getQuietHoursStart());
    }
    if (request.getQuietHoursEnd() != null) {
      pref.setQuietHoursEnd(request.getQuietHoursEnd());
    }
    if (request.getQuietHoursEnabled() != null) {
      pref.setQuietHoursEnabled(request.getQuietHoursEnabled());
    }
    if (request.getMaxNotificationsPerHour() != null) {
      pref.setMaxNotificationsPerHour(request.getMaxNotificationsPerHour());
    }
    
    pref.setUpdatedTimeUnix(Instant.now().getEpochSecond());
    
    NotificationPreference saved = preferenceRepository.save(pref);
    log.info("[NotificationPreferenceService] Updated preferences for user {}", userId);
    
    return toDTO(saved);
  }
  
  /**
   * Create default preferences for new user
   */
  private NotificationPreference createDefaultPreferences(String userId) {
    Map<String, Boolean> defaultEnabledEventTypes = new HashMap<>();
    defaultEnabledEventTypes.put("WATER_REMINDER", true);
    defaultEnabledEventTypes.put("ACTIVITY_COMPLETED", true);
    defaultEnabledEventTypes.put("FALL_DETECTED", true);
    defaultEnabledEventTypes.put("HIGH_HEART_RATE", true);
    defaultEnabledEventTypes.put("LOW_SLEEP_ALERT", true);
    defaultEnabledEventTypes.put("APPOINTMENT_REMINDER", true);
    defaultEnabledEventTypes.put("MEDICATION_TIME", true);
    
    long now = Instant.now().getEpochSecond();
    
    NotificationPreference pref = NotificationPreference.builder()
        .userId(userId)
        .allNotificationsEnabled(true)
        .preferredLanguage("vi")
        .timezone("Asia/Ho_Chi_Minh")
        .pushEnabled(true)
        .emailEnabled(true)
        .inAppEnabled(true)
        .enabledEventTypes(defaultEnabledEventTypes)
        .quietHoursEnabled(false)
        .quietHoursStart("22:00")
        .quietHoursEnd("08:00")
        .maxNotificationsPerHour(0)  // unlimited
        .createdTimeUnix(now)
        .updatedTimeUnix(now)
        .build();
    
    NotificationPreference saved = preferenceRepository.save(pref);
    log.info("[NotificationPreferenceService] Created default preferences for user {}", userId);
    
    return saved;
  }
  
  /**
   * Check if a notification type is enabled for user
   */
  public boolean isEventTypeEnabled(String userId, String eventType) {
    return preferenceRepository.findByUserId(userId)
        .map(pref -> {
          if (!pref.getAllNotificationsEnabled()) {
            return false;
          }
          Map<String, Boolean> enabledTypes = pref.getEnabledEventTypes();
          return enabledTypes == null || enabledTypes.getOrDefault(eventType, true);
        })
        .orElse(true);  // Default to enabled if no preferences
  }
  
  /**
   * Convert NotificationPreference to DTO
   */
  private NotificationPreferenceDTO toDTO(NotificationPreference pref) {
    return NotificationPreferenceDTO.builder()
        .userId(pref.getUserId())
        .allNotificationsEnabled(pref.getAllNotificationsEnabled())
        .preferredLanguage(pref.getPreferredLanguage())
        .timezone(pref.getTimezone())
        .pushEnabled(pref.getPushEnabled())
        .emailEnabled(pref.getEmailEnabled())
        .inAppEnabled(pref.getInAppEnabled())
        .enabledEventTypes(pref.getEnabledEventTypes())
        .quietHoursStart(pref.getQuietHoursStart())
        .quietHoursEnd(pref.getQuietHoursEnd())
        .quietHoursEnabled(pref.getQuietHoursEnabled())
        .maxNotificationsPerHour(pref.getMaxNotificationsPerHour())
        .build();
  }
}

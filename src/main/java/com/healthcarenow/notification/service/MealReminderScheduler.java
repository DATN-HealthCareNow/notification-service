package com.healthcarenow.notification.service;

import com.healthcarenow.notification.dto.NotificationEvent;
import com.healthcarenow.notification.model.NotificationPreference;
import com.healthcarenow.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealReminderScheduler {

  private static final String EVENT_TYPE = "MEAL_REMINDER";

  private final NotificationPreferenceRepository preferenceRepository;
  private final NotificationHandler notificationHandler;

  private final Set<String> dispatchedKeys = ConcurrentHashMap.newKeySet();

  @Value("${notification.meal-reminder.enabled:true}")
  private boolean enabled;

  @Scheduled(
      fixedDelayString = "${notification.meal-reminder.check-interval-ms:60000}",
      initialDelayString = "${notification.meal-reminder.initial-delay-ms:45000}")
  public void sendMealReminders() {
    if (!enabled) {
      return;
    }

    List<NotificationPreference> preferences = preferenceRepository.findAll();
    if (preferences.isEmpty()) {
      return;
    }

    for (NotificationPreference preference : preferences) {
      if (preference == null || preference.getUserId() == null || preference.getUserId().isBlank()) {
        continue;
      }

      if (Boolean.FALSE.equals(preference.getAllNotificationsEnabled())) {
        continue;
      }

      ZonedDateTime now = ZonedDateTime.now(resolveZoneId(preference.getTimezone()));
      int currentHour = now.getHour();
      
      String mealType = null;
      String mealNameVi = null;
      String mealNameEn = null;
      
      // Breakfast: 7:00 AM - 8:00 AM
      if (currentHour == 7) {
        mealType = "BREAKFAST";
        mealNameVi = "Bữa sáng";
        mealNameEn = "Breakfast";
      } 
      // Lunch: 12:00 PM - 1:00 PM
      else if (currentHour == 12) {
        mealType = "LUNCH";
        mealNameVi = "Bữa trưa";
        mealNameEn = "Lunch";
      } 
      // Dinner: 18:00 PM - 19:00 PM
      else if (currentHour == 18) {
        mealType = "DINNER";
        mealNameVi = "Bữa tối";
        mealNameEn = "Dinner";
      } else {
        continue;
      }

      String dispatchKey = preference.getUserId() + ":" + now.toLocalDate() + ":" + mealType;
      if (!dispatchedKeys.add(dispatchKey)) {
        continue; // Already sent for this meal today
      }

      String language = preference.getPreferredLanguage() == null ? "vi" : preference.getPreferredLanguage();

      Map<String, Object> payload = new HashMap<>();
      payload.put("language", language);
      payload.put("meal_name", "vi".equalsIgnoreCase(language) ? mealNameVi : mealNameEn);
      
      if ("vi".equalsIgnoreCase(language)) {
        payload.put("title", "Đến giờ " + mealNameVi.toLowerCase() + " rồi! 🍽️");
        payload.put("body", "Đã đến lúc nạp năng lượng cho " + mealNameVi.toLowerCase() + ". Đừng bỏ bữa bạn nhé!");
      } else {
        payload.put("title", "Time for " + mealNameEn.toLowerCase() + "! 🍽️");
        payload.put("body", "It's time for your " + mealNameEn.toLowerCase() + ". Don't skip your meal!");
      }
      
      payload.put("reminder_source", "meal-reminder-scheduler");

      NotificationEvent event = NotificationEvent.builder()
          .eventType(EVENT_TYPE)
          .userId(preference.getUserId())
          .priority("NORMAL")
          .payload(payload)
          .build();

      try {
        notificationHandler.processEvent(event);
        log.info("[MealReminderScheduler] {} reminder dispatched for user {}", mealType, preference.getUserId());
      } catch (Exception ex) {
        log.error("[MealReminderScheduler] Failed to dispatch {} reminder for user {}", mealType, preference.getUserId(), ex);
      }
    }
  }

  private ZoneId resolveZoneId(String timezone) {
    try {
      return timezone == null || timezone.isBlank() ? ZoneId.of("Asia/Ho_Chi_Minh") : ZoneId.of(timezone);
    } catch (Exception ex) {
      return ZoneId.of("Asia/Ho_Chi_Minh");
    }
  }
}

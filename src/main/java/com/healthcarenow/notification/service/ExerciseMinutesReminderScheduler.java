package com.healthcarenow.notification.service;

import com.healthcarenow.notification.client.IotServiceClient;
import com.healthcarenow.notification.dto.ExerciseMetricsResponse;
import com.healthcarenow.notification.dto.NotificationEvent;
import com.healthcarenow.notification.model.NotificationPreference;
import com.healthcarenow.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
public class ExerciseMinutesReminderScheduler {

  private static final String EVENT_TYPE = "LOW_EXERCISE_REMINDER";

  private final NotificationPreferenceRepository preferenceRepository;
  private final NotificationHandler notificationHandler;
  private final IotServiceClient iotServiceClient;

  private final Set<String> dispatchedKeys = ConcurrentHashMap.newKeySet();

  @Value("${app.internal-token:hcn-internal-secret-2024}")
  private String internalApiToken;

  @Value("${notification.exercise-minutes-reminder.enabled:true}")
  private boolean enabled;

  @Value("${notification.exercise-minutes-reminder.reminder-hour:7}")
  private int reminderHour;

  @Value("${notification.exercise-minutes-reminder.window-minutes:10}")
  private int windowMinutes;

  @Value("${notification.exercise-minutes-reminder.threshold-minutes:30}")
  private int thresholdMinutes;

  @Scheduled(
      fixedDelayString = "${notification.exercise-minutes-reminder.check-interval-ms:60000}",
      initialDelayString = "${notification.exercise-minutes-reminder.initial-delay-ms:20000}")
  public void sendLowExerciseReminder() {
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

      Map<String, Boolean> enabledEventTypes = preference.getEnabledEventTypes();
      if (enabledEventTypes != null && Boolean.FALSE.equals(enabledEventTypes.get(EVENT_TYPE))) {
        continue;
      }

      ZoneId userZone = resolveZoneId(preference.getTimezone());
      ZonedDateTime now = ZonedDateTime.now(userZone);

      if (now.getHour() != reminderHour || now.getMinute() >= windowMinutes) {
        continue;
      }

      LocalDate targetDate = now.toLocalDate().minusDays(1);
      String targetDateString = targetDate.toString();

      String dispatchKey = preference.getUserId() + ":" + targetDateString + ":" + EVENT_TYPE;
      if (!dispatchedKeys.add(dispatchKey)) {
        continue;
      }

      ExerciseMetricsResponse metrics;
      try {
        metrics = iotServiceClient.getExerciseMetrics(internalApiToken, preference.getUserId(), targetDateString);
      } catch (Exception ex) {
        log.warn("[ExerciseMinutesReminderScheduler] Failed to fetch exercise metrics for user {} and date {}",
            preference.getUserId(), targetDateString, ex);
        continue;
      }

      int exerciseMinutes = metrics != null && metrics.getExerciseMinutes() != null ? metrics.getExerciseMinutes() : 0;
      if (exerciseMinutes >= thresholdMinutes) {
        continue;
      }

      int missingMinutes = Math.max(thresholdMinutes - exerciseMinutes, 0);
      String language = preference.getPreferredLanguage() == null ? "vi" : preference.getPreferredLanguage();

      Map<String, String> payload = new HashMap<>();
      payload.put("language", language);
      payload.put("exercise_minutes", String.valueOf(exerciseMinutes));
      payload.put("target_minutes", String.valueOf(thresholdMinutes));
      payload.put("missing_minutes", String.valueOf(missingMinutes));
      payload.put("date", targetDateString);
      payload.put("reminder_source", "exercise-minutes-reminder-scheduler");

      NotificationEvent event = NotificationEvent.builder()
          .eventType(EVENT_TYPE)
          .userId(preference.getUserId())
          .priority("NORMAL")
          .payload(payload)
          .build();

      try {
        notificationHandler.processEvent(event);
        log.info("[ExerciseMinutesReminderScheduler] Reminder dispatched for user {} with exercise {} min",
            preference.getUserId(), exerciseMinutes);
      } catch (Exception ex) {
        log.error("[ExerciseMinutesReminderScheduler] Failed to dispatch reminder for user {}",
            preference.getUserId(), ex);
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

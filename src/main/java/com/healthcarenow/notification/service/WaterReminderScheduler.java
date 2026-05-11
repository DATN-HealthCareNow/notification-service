package com.healthcarenow.notification.service;

import com.healthcarenow.notification.client.CoreServiceClient;
import com.healthcarenow.notification.dto.NotificationEvent;
import com.healthcarenow.notification.model.NotificationPreference;
import com.healthcarenow.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaterReminderScheduler {

  private static final String EVENT_TYPE = "WATER_REMINDER";

  private final NotificationPreferenceRepository preferenceRepository;
  private final NotificationHandler notificationHandler;
  private final CoreServiceClient coreServiceClient;

  private final Set<String> dispatchedKeys = ConcurrentHashMap.newKeySet();

  @Value("${app.internal-token:hcn-internal-secret-2024}")
  private String internalApiToken;

  @Value("${notification.water-reminder.enabled:true}")
  private boolean enabled;

  @Value("${notification.water-reminder.windows:7-8,11-12,17-18}")
  private String reminderWindows;

  @Scheduled(
      fixedDelayString = "${notification.water-reminder.check-interval-ms:60000}",
      initialDelayString = "${notification.water-reminder.initial-delay-ms:30000}")
  public void sendWaterReminders() {
    if (!enabled) {
      log.debug("[WaterReminderScheduler] Water reminder scheduler is disabled");
      return;
    }

    List<NotificationPreference> preferences = preferenceRepository.findAll();
    if (preferences.isEmpty()) {
      log.info("[WaterReminderScheduler] No notification preferences found, skipping water reminders");
      return;
    }

    log.info("[WaterReminderScheduler] Starting water reminder batch for {} users", preferences.size());

    List<WaterWindow> configuredWindows = parseConfiguredWindows(reminderWindows);

    for (NotificationPreference preference : preferences) {
      if (preference == null || preference.getUserId() == null || preference.getUserId().isBlank()) {
        continue;
      }

      if (Boolean.FALSE.equals(preference.getAllNotificationsEnabled())) {
        continue;
      }

      ZonedDateTime now = ZonedDateTime.now(resolveZoneId(preference.getTimezone()));
      Map<String, Boolean> enabledEventTypes = preference.getEnabledEventTypes();
      if (enabledEventTypes != null && Boolean.FALSE.equals(enabledEventTypes.get(EVENT_TYPE))) {
        continue;
      }

      WindowDispatchPlan dispatchPlan = resolveDispatchPlan(preference.getUserId(), now.toLocalDate(), now.toLocalDateTime(), configuredWindows);
      if (dispatchPlan == null) {
        continue;
      }

      if (now.toLocalDateTime().isBefore(dispatchPlan.scheduledAt())) {
        continue;
      }

      String dispatchKey = preference.getUserId() + ":" + now.toLocalDate() + ":" + dispatchPlan.windowKey();
      if (!dispatchedKeys.add(dispatchKey)) {
        continue;
      }

      Map<String, Object> waterProgress;
      try {
        waterProgress = coreServiceClient.getWaterProgress(internalApiToken, preference.getUserId());
      } catch (Exception ex) {
        log.warn("[WaterReminderScheduler] Failed to fetch water progress for user {}",
            preference.getUserId(), ex);
        dispatchedKeys.remove(dispatchKey);
        continue;
      }

      String language = preference.getPreferredLanguage() == null ? "vi" : preference.getPreferredLanguage();
      int currentMl = readInt(waterProgress, "totalTodayMl");
      int goalMl = readInt(waterProgress, "goalMl");

      // Default goal 2000ml nếu user chưa cài đặt mục tiêu
      if (goalMl <= 0) {
        goalMl = 2000;
      }

      int neededMl = Math.max(goalMl - currentMl, 0);

      if (neededMl <= 0) {
        log.info("[WaterReminderScheduler] Skip user {} — daily water goal already met ({}/{}ml)",
            preference.getUserId(), currentMl, goalMl);
        continue;
      }

      Map<String, Object> payload = new HashMap<>();
      payload.put("language", language);
      payload.put("current", String.valueOf(currentMl));
      payload.put("goal", String.valueOf(goalMl));
      payload.put("needed", String.valueOf(neededMl));
      // title & body luôn có để NotificationHandler fallback hoạt động
      payload.put("title", "Đã đến giờ uống nước! 💧");
      payload.put("body", String.format("Bạn cần uống thêm %d ml nước nữa để đạt mục tiêu ngày hôm nay (%d/%d ml).",
          neededMl, currentMl, goalMl));
      payload.put("reminder_source", "water-reminder-scheduler");
      payload.put("reminder_interval_windows", reminderWindows);
      payload.put("scheduled_window", dispatchPlan.windowKey());
      payload.put("scheduled_time", dispatchPlan.scheduledAt().toString());
      payload.put("scheduled_timezone", now.getZone().getId());


      NotificationEvent event = NotificationEvent.builder()
          .eventType(EVENT_TYPE)
          .userId(preference.getUserId())
          .priority("NORMAL")
          .payload(payload)
          .build();

      try {
        notificationHandler.processEvent(event);
        log.info("[WaterReminderScheduler] Water reminder dispatched for user {}", preference.getUserId());
      } catch (Exception ex) {
        log.error("[WaterReminderScheduler] Failed to dispatch water reminder for user {}",
            preference.getUserId(), ex);
      }
    }
  }

  @Scheduled(cron = "0 50 23 * * *", zone = "Asia/Ho_Chi_Minh")
  public void sendDailyWaterSummary() {
    if (!enabled) return;
    List<NotificationPreference> preferences = preferenceRepository.findAll();
    for (NotificationPreference preference : preferences) {
      if (preference == null || preference.getUserId() == null || preference.getUserId().isBlank()) continue;
      if (Boolean.FALSE.equals(preference.getAllNotificationsEnabled())) continue;

      Map<String, Boolean> enabledEventTypes = preference.getEnabledEventTypes();
      if (enabledEventTypes != null && Boolean.FALSE.equals(enabledEventTypes.get(EVENT_TYPE))) continue;

      Map<String, Object> waterProgress;
      try {
        waterProgress = coreServiceClient.getWaterProgress(internalApiToken, preference.getUserId());
      } catch (Exception ex) {
        continue;
      }

      int currentMl = readInt(waterProgress, "totalTodayMl");
      int goalMl = readInt(waterProgress, "goalMl");
      if (goalMl <= 0) goalMl = 2000;
      
      int neededMl = Math.max(goalMl - currentMl, 0);
      if (neededMl <= 0) continue; // Passed goal, no need to remind

      String language = preference.getPreferredLanguage() == null ? "vi" : preference.getPreferredLanguage();
      Map<String, Object> payload = new HashMap<>();
      payload.put("language", language);
      payload.put("current", String.valueOf(currentMl));
      payload.put("goal", String.valueOf(goalMl));
      payload.put("needed", String.valueOf(neededMl));
      
      if ("vi".equalsIgnoreCase(language)) {
        payload.put("title", "Tổng kết nước uống hôm nay 💧");
        payload.put("body", String.format("Bạn còn thiếu %d ml nước để đạt mục tiêu %d ml. Hãy uống một ly trước khi đi ngủ nhé!", neededMl, goalMl));
      } else {
        payload.put("title", "Daily Water Summary 💧");
        payload.put("body", String.format("You are %d ml short of your %d ml goal. Have a glass of water before bed!", neededMl, goalMl));
      }

      payload.put("reminder_source", "water-summary-scheduler");

      NotificationEvent event = NotificationEvent.builder()
          .eventType("WATER_SUMMARY_REMINDER")
          .userId(preference.getUserId())
          .priority("NORMAL")
          .payload(payload)
          .build();

      try {
        notificationHandler.processEvent(event);
      } catch (Exception ex) {
        log.error("Failed to dispatch water summary reminder", ex);
      }
    }
  }

  private List<WaterWindow> parseConfiguredWindows(String rawWindows) {
    List<WaterWindow> windows = new ArrayList<>();
    if (rawWindows == null || rawWindows.isBlank()) {
      windows.add(new WaterWindow(7, 8));
      windows.add(new WaterWindow(11, 12));
      windows.add(new WaterWindow(17, 18));
      return windows;
    }

    for (String rawWindow : rawWindows.split(",")) {
      String trimmed = rawWindow.trim();
      if (trimmed.isBlank() || !trimmed.contains("-")) {
        continue;
      }

      String[] parts = trimmed.split("-");
      if (parts.length != 2) {
        continue;
      }

      try {
        int startHour = Integer.parseInt(parts[0].trim());
        int endHour = Integer.parseInt(parts[1].trim());
        if (startHour < 0 || endHour > 23 || startHour > endHour) {
          continue;
        }
        windows.add(new WaterWindow(startHour, endHour));
      } catch (NumberFormatException ignored) {
        // skip invalid config
      }
    }

    if (windows.isEmpty()) {
      windows.add(new WaterWindow(7, 8));
      windows.add(new WaterWindow(11, 12));
      windows.add(new WaterWindow(17, 18));
    }

    return windows;
  }

  private WindowDispatchPlan resolveDispatchPlan(String userId, java.time.LocalDate date, java.time.LocalDateTime currentTime,
      List<WaterWindow> windows) {
    for (WaterWindow window : windows) {
      WindowDispatchPlan plan = window.buildPlan(userId, date);
      if (plan.isWithinWindow(currentTime)) {
        return plan;
      }
    }
    return null;
  }

  private int readInt(Map<String, Object> payload, String key) {
    if (payload == null || !payload.containsKey(key) || payload.get(key) == null) {
      return 0;
    }

    Object value = payload.get(key);
    if (value instanceof Number number) {
      return number.intValue();
    }

    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  private ZoneId resolveZoneId(String timezone) {
    try {
      return timezone == null || timezone.isBlank() ? ZoneId.of("Asia/Ho_Chi_Minh") : ZoneId.of(timezone);
    } catch (Exception ex) {
      return ZoneId.of("Asia/Ho_Chi_Minh");
    }
  }

  private static final class WaterWindow {
    private final int startHour;
    private final int endHour;

    private WaterWindow(int startHour, int endHour) {
      this.startHour = startHour;
      this.endHour = endHour;
    }

    private WindowDispatchPlan buildPlan(String userId, java.time.LocalDate date) {
      LocalDateTime start = date.atTime(LocalTime.of(startHour, 0));
      LocalDateTime endExclusive = date.atTime(LocalTime.of(endHour + 1, 0));
      int totalMinutes = (int) Duration.between(start, endExclusive).toMinutes();
      int offsetMinutes = Math.floorMod(Objects.hash(userId, date.toString(), windowKey()), totalMinutes);
      LocalDateTime scheduledAt = start.plusMinutes(offsetMinutes);
      return new WindowDispatchPlan(windowKey(), scheduledAt, endExclusive);
    }

    private String windowKey() {
      return startHour + "-" + endHour;
    }
  }

  private record WindowDispatchPlan(String windowKey, LocalDateTime scheduledAt, LocalDateTime endExclusive) {
    private boolean isWithinWindow(LocalDateTime currentTime) {
      return !currentTime.isBefore(scheduledAt) && currentTime.isBefore(endExclusive);
    }
  }
}

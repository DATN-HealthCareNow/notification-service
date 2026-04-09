package com.healthcarenow.notification.controller;

import com.healthcarenow.notification.dto.NotificationLogDTO;
import com.healthcarenow.notification.dto.NotificationPreferenceDTO;
import com.healthcarenow.notification.service.NotificationLogService;
import com.healthcarenow.notification.service.NotificationPreferenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Notification API endpoints for mobile/web clients
 * All endpoints require authentication via x-user-id header (set by Nginx
 * auth_request)
 */

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

  private final NotificationLogService notificationLogService;
  private final NotificationPreferenceService notificationPreferenceService;

  /**
   * GET /api/v1/notifications
   * Fetch paginated notification logs for current user
   * Query params: page=0, size=20, sort by createdAt DESC
   */
  @GetMapping
  public ResponseEntity<Page<NotificationLogDTO>> getNotifications(
      @RequestHeader("x-user-id") String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.info("[NotificationController] GET /notifications userId={}, page={}, size={}", userId, page, size);

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<NotificationLogDTO> notifications = notificationLogService.getUserNotifications(userId, pageRequest);

    return ResponseEntity.ok(notifications);
  }

  /**
   * GET /api/v1/notifications/unread
   * Fetch only unread notifications for current user
   */
  @GetMapping("/unread")
  public ResponseEntity<Page<NotificationLogDTO>> getUnreadNotifications(
      @RequestHeader("x-user-id") String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.info("[NotificationController] GET /notifications/unread userId={}", userId);

    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<NotificationLogDTO> unreadNotifications = notificationLogService.getUnreadNotifications(userId, pageRequest);

    return ResponseEntity.ok(unreadNotifications);
  }

  /**
   * GET /api/v1/notifications/unread-count
   * Get count of unread notifications
   */
  @GetMapping("/unread-count")
  public ResponseEntity<UnreadCountResponse> getUnreadCount(
      @RequestHeader("x-user-id") String userId) {

    log.info("[NotificationController] GET /notifications/unread-count userId={}", userId);

    long unreadCount = notificationLogService.getUnreadCount(userId);

    return ResponseEntity.ok(UnreadCountResponse.builder()
        .userId(userId)
        .unreadCount(unreadCount)
        .build());
  }

  /**
   * PATCH /api/v1/notifications/{notificationId}/read
   * Mark a single notification as read
   */
  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<NotificationLogDTO> markAsRead(
      @RequestHeader("x-user-id") String userId,
      @PathVariable String notificationId) {

    log.info("[NotificationController] PATCH /notifications/{}/read userId={}", notificationId, userId);

    NotificationLogDTO updated = notificationLogService.markAsRead(userId, notificationId);

    return ResponseEntity.ok(updated);
  }

  /**
   * PATCH /api/v1/notifications/read-all
   * Mark all notifications as read
   */
  @PatchMapping("/read-all")
  public ResponseEntity<ReadAllResponse> markAllAsRead(
      @RequestHeader("x-user-id") String userId) {

    log.info("[NotificationController] PATCH /notifications/read-all userId={}", userId);

    long updatedCount = notificationLogService.markAllAsRead(userId);

    return ResponseEntity.ok(ReadAllResponse.builder()
        .userId(userId)
        .updatedCount(updatedCount)
        .build());
  }

  /**
   * GET /api/v1/notifications/preferences
   * Get user notification preferences (enable/disable per type)
   */
  @GetMapping("/preferences")
  public ResponseEntity<NotificationPreferenceDTO> getPreferences(
      @RequestHeader("x-user-id") String userId) {

    log.info("[NotificationController] GET /notifications/preferences userId={}", userId);

    NotificationPreferenceDTO preferences = notificationPreferenceService.getOrCreatePreferences(userId);

    return ResponseEntity.ok(preferences);
  }

  /**
   * PATCH /api/v1/notifications/preferences
   * Update user notification preferences
   */
  @PatchMapping("/preferences")
  public ResponseEntity<NotificationPreferenceDTO> updatePreferences(
      @RequestHeader("x-user-id") String userId,
      @RequestBody NotificationPreferenceDTO request) {

    log.info("[NotificationController] PATCH /notifications/preferences userId={}", userId);

    NotificationPreferenceDTO updated = notificationPreferenceService.updatePreferences(userId, request);

    return ResponseEntity.ok(updated);
  }

  // DTO Response classes

  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class UnreadCountResponse {
    private String userId;
    private Long unreadCount;
  }

  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class ReadAllResponse {
    private String userId;
    private Long updatedCount;
  }

}

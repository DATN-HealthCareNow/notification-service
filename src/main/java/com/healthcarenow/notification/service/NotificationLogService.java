package com.healthcarenow.notification.service;

import com.healthcarenow.notification.dto.NotificationLogDTO;
import com.healthcarenow.notification.model.NotificationLog;
import com.healthcarenow.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationLogService {
  
  private final NotificationLogRepository notificationLogRepository;

  /**
   * Event IDs that should never appear in the user's in-app notification list.
   * - WATER_REMINDER: transient nudges handled separately
   * - Any event whose ID contains "OTP": these are auth emails, not in-app messages
   */
  private static final List<String> EXCLUDED_EVENT_IDS = Arrays.asList(
      "WATER_REMINDER",
      "FORGOT_PASSWORD_OTP",
      "REGISTER_OTP",
      "CHANGE_PASSWORD_OTP",
      "OTP"
  );

  /**
   * Get all in-app notifications for a user (paginated), excluding system/OTP events.
   */
  public Page<NotificationLogDTO> getUserNotifications(String userId, Pageable pageable) {
    Page<NotificationLog> page = notificationLogRepository
        .findByUserIdAndEventIdNotIn(userId, EXCLUDED_EVENT_IDS, pageable);
    List<NotificationLogDTO> dtos = page.getContent().stream()
        .filter(n -> n.getEventId() == null || !n.getEventId().toUpperCase().contains("OTP"))
        .map(this::toDTO)
        .collect(Collectors.toList());
    return new PageImpl<>(dtos, pageable, page.getTotalElements());
  }
  
  /**
   * Get only unread in-app notifications for a user, excluding system/OTP events.
   */
  public Page<NotificationLogDTO> getUnreadNotifications(String userId, Pageable pageable) {
    Page<NotificationLog> page = notificationLogRepository
        .findByUserIdAndEventIdNotInAndIsRead(userId, EXCLUDED_EVENT_IDS, false, pageable);
    List<NotificationLogDTO> dtos = page.getContent().stream()
        .filter(n -> n.getEventId() == null || !n.getEventId().toUpperCase().contains("OTP"))
        .map(this::toDTO)
        .collect(Collectors.toList());
    return new PageImpl<>(dtos, pageable, page.getTotalElements());
  }
  
  /**
   * Get count of unread in-app notifications, excluding system/OTP events.
   */
  public Long getUnreadCount(String userId) {
    long count = notificationLogRepository
        .countByUserIdAndEventIdNotInAndIsRead(userId, EXCLUDED_EVENT_IDS, false);
    return count;
  }
  
  /**
   * Mark a single notification as read.
   */
  public NotificationLogDTO markAsRead(String userId, String notificationId) {
    NotificationLog notification = notificationLogRepository.findById(notificationId)
        .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
    
    // Verify ownership
    if (!notification.getUserId().equals(userId)) {
      throw new RuntimeException("Unauthorized - notification belongs to another user");
    }
    
    notification.setIsRead(true);
    notification.setReadAt(LocalDateTime.now());
    
    NotificationLog saved = notificationLogRepository.save(notification);
    log.info("[NotificationLogService] Marked notification {} as read", notificationId);
    
    return toDTO(saved);
  }
  
  /**
   * Mark all in-app notifications (excluding OTP/system) as read for a user.
   * Uses a direct list query instead of paging to avoid Integer.MAX_VALUE issues.
   */
  public Long markAllAsRead(String userId) {
    List<NotificationLog> unreadNotifications = notificationLogRepository
        .findAllUnreadExcluding(userId, EXCLUDED_EVENT_IDS);

    // Extra safety: also skip any that slipped through with OTP in eventId
    List<NotificationLog> toMark = unreadNotifications.stream()
        .filter(n -> n.getEventId() == null || !n.getEventId().toUpperCase().contains("OTP"))
        .collect(Collectors.toList());

    LocalDateTime now = LocalDateTime.now();
    toMark.forEach(notif -> {
      notif.setIsRead(true);
      notif.setReadAt(now);
    });
    
    notificationLogRepository.saveAll(toMark);
    log.info("[NotificationLogService] Marked {} notifications as read for user {}", toMark.size(), userId);
    
    return (long) toMark.size();
  }
  
  /**
   * Convert NotificationLog to DTO
   */
  private NotificationLogDTO toDTO(NotificationLog log) {
    return NotificationLogDTO.builder()
        .id(log.getId())
        .userId(log.getUserId())
        .type(log.getType())
        .eventId(log.getEventId())
        .title(log.getTitle())
        .content(log.getContent())
        .status(log.getStatus())
        .priority(log.getPriority())
        .isRead(log.getIsRead() != null ? log.getIsRead() : false)
        .createdAt(log.getCreatedAt())
        .sentAt(log.getSentAt())
        .readAt(log.getReadAt())
        .build();
  }
}


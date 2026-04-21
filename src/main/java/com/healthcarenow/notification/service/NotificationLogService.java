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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationLogService {
  
  private final NotificationLogRepository notificationLogRepository;
  
  /**
   * Get all notifications for a user (paginated), excluding WATER_REMINDER
   */
  public Page<NotificationLogDTO> getUserNotifications(String userId, Pageable pageable) {
    Page<NotificationLog> page = notificationLogRepository.findByUserIdAndEventIdNot(userId, "WATER_REMINDER", pageable);
    List<NotificationLogDTO> dtos = page.getContent().stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
    return new PageImpl<>(dtos, pageable, page.getTotalElements());
  }
  
  /**
   * Get only unread notifications for a user, excluding WATER_REMINDER
   */
  public Page<NotificationLogDTO> getUnreadNotifications(String userId, Pageable pageable) {
    Page<NotificationLog> page = notificationLogRepository.findByUserIdAndEventIdNotAndIsRead(userId, "WATER_REMINDER", false, pageable);
    List<NotificationLogDTO> dtos = page.getContent().stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
    return new PageImpl<>(dtos, pageable, page.getTotalElements());
  }
  
  /**
   * Get count of unread notifications, excluding WATER_REMINDER
   */
  public Long getUnreadCount(String userId) {
    return notificationLogRepository.countByUserIdAndEventIdNotAndIsRead(userId, "WATER_REMINDER", false);
  }
  
  /**
   * Mark a single notification as read
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
  
  public Long markAllAsRead(String userId) {
    Page<NotificationLog> unreadPage = notificationLogRepository.findByUserIdAndEventIdNotAndIsRead(userId, 
        "WATER_REMINDER", false,
        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));
    List<NotificationLog> unreadNotifications = unreadPage.getContent();
    LocalDateTime now = LocalDateTime.now();
    
    unreadNotifications.forEach(notif -> {
      notif.setIsRead(true);
      notif.setReadAt(now);
    });
    
    notificationLogRepository.saveAll(unreadNotifications);
    log.info("[NotificationLogService] Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
    
    return (long) unreadNotifications.size();
  }
  
  /**
   * Convert NotificationLog to DTO
   */
  private NotificationLogDTO toDTO(NotificationLog log) {
    return NotificationLogDTO.builder()
        .id(log.getId())
        .userId(log.getUserId())
        .type(log.getType())
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

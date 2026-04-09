package com.healthcarenow.notification.controller;

import com.healthcarenow.notification.model.NotificationLog;
import com.healthcarenow.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationLogRepository repository;

    @GetMapping
    public ResponseEntity<Page<NotificationLog>> getNotifications(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching notifications for user {} (page: {}, size: {})", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationLog> logs = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        long count = repository.countByUserIdAndIsReadFalse(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("userId", userId);
        res.put("unreadCount", count);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationLog> markAsRead(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @PathVariable String id) {
        log.info("Marking notification {} as read for user {}", id, userId);
        var opt = repository.findById(id);
        if (opt.isPresent()) {
            NotificationLog notif = opt.get();
            if (notif.getUserId().equals(userId)) {
                notif.setRead(true);
                notif.setReadAt(LocalDateTime.now());
                repository.save(notif);
                return ResponseEntity.ok(notif);
            } else {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        log.info("Marking all notifications as read for user {}", userId);
        // Find all unread without pagination
        Pageable unpaged = Pageable.unpaged(); 
        Page<NotificationLog> unreadLogs = repository.findByUserIdOrderByCreatedAtDesc(userId, unpaged);
        
        List<NotificationLog> all = unreadLogs.getContent();
        boolean changed = false;
        for (NotificationLog logItem : all) {
            if (!logItem.isRead()) {
                logItem.setRead(true);
                logItem.setReadAt(LocalDateTime.now());
                changed = true;
            }
        }
        
        if (changed) {
            repository.saveAll(all);
        }
        
        return ResponseEntity.ok().build();
    }
}

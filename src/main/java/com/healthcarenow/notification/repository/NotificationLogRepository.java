package com.healthcarenow.notification.repository;

import com.healthcarenow.notification.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
  Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
  long countByUserIdAndIsReadFalse(String userId);
}

package com.healthcarenow.notification.repository;

import com.healthcarenow.notification.model.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
	Page<NotificationLog> findByUserId(String userId, Pageable pageable);

	Page<NotificationLog> findByUserIdAndIsReadFalse(String userId, Pageable pageable);

	long countByUserIdAndIsReadFalse(String userId);
}

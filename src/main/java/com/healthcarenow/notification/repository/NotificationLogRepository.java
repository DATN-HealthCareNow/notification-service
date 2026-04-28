package com.healthcarenow.notification.repository;

import com.healthcarenow.notification.model.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

	// Exclude a single eventId (kept for backward compat)
	Page<NotificationLog> findByUserIdAndEventIdNot(String userId, String eventId, Pageable pageable);

	Page<NotificationLog> findByUserIdAndEventIdNotAndIsRead(String userId, String eventId, Boolean isRead, Pageable pageable);

	long countByUserIdAndEventIdNotAndIsRead(String userId, String eventId, Boolean isRead);

	// Exclude multiple eventIds
	Page<NotificationLog> findByUserIdAndEventIdNotIn(String userId, List<String> excludedEventIds, Pageable pageable);

	Page<NotificationLog> findByUserIdAndEventIdNotInAndIsRead(String userId, List<String> excludedEventIds, Boolean isRead, Pageable pageable);

	long countByUserIdAndEventIdNotInAndIsRead(String userId, List<String> excludedEventIds, Boolean isRead);

	// All unread for bulk mark-as-read (no paging needed)
	@Query("{ 'userId': ?0, 'isRead': false, 'eventId': { $nin: ?1 } }")
	List<NotificationLog> findAllUnreadExcluding(String userId, List<String> excludedEventIds);

	@org.springframework.data.mongodb.repository.Update("{ '$set': { 'isRead': true, 'readAt': ?2 } }")
	@Query("{ 'userId': ?0, 'isRead': false, 'eventId': { $nin: ?1 } }")
	long markAllAsReadForUser(String userId, List<String> excludedEventIds, LocalDateTime now);
}


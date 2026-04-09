package com.healthcarenow.notification.repository;

import com.healthcarenow.notification.model.NotificationPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {
  
  // Find preference by user ID
  Optional<NotificationPreference> findByUserId(String userId);
  
  // Check if user exists in preferences
  boolean existsByUserId(String userId);
}

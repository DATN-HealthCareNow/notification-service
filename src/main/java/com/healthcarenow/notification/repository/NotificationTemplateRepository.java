package com.healthcarenow.notification.repository;

import com.healthcarenow.notification.model.NotificationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {
  Optional<NotificationTemplate> findByCodeAndTypeAndLanguage(String code, String type, String language);

  Optional<NotificationTemplate> findByCodeAndType(String code, String type);
}

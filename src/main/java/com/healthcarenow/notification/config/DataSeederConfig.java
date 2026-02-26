package com.healthcarenow.notification.config;

import com.healthcarenow.notification.model.NotificationTemplate;
import com.healthcarenow.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeederConfig {

  private final NotificationTemplateRepository templateRepository;

  @Bean
  public CommandLineRunner initData() {
    return args -> {
      log.info("Checking Notification Templates in MongoDB...");

      // Khởi tạo Email Template nếu chưa có
      if (templateRepository.findByCodeAndTypeAndLanguage("EMERGENCY_FALL", "EMAIL", "vi").isEmpty()) {
        log.info("Seeding default EMAIL template for EMERGENCY_FALL (vi)...");
        NotificationTemplate emailTemplate = NotificationTemplate.builder()
            .code("EMERGENCY_FALL")
            .type("EMAIL")
            .language("vi")
            .isCritical(true)
            .title("Cảnh báo khẩn cấp: TÉ NGÃ")
            .body("<h2>Cảm biến IoT Phát hiện té ngã!</h2>" +
                "<p>Hệ thống ghi nhận <b>{name}</b> vừa bị té ngã tại khu vực: <b>{location}</b>.</p>" +
                "<p>Đây là cảnh báo gửi tự động, vui lòng kiểm tra ngay lập tức!</p>")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        templateRepository.save(emailTemplate);
      }

      // Khởi tạo Push Template tương ứng nếu chưa có
      if (templateRepository.findByCodeAndTypeAndLanguage("EMERGENCY_FALL", "PUSH", "vi").isEmpty()) {
        log.info("Seeding default PUSH template for EMERGENCY_FALL (vi)...");
        NotificationTemplate pushTemplate = NotificationTemplate.builder()
            .code("EMERGENCY_FALL")
            .type("PUSH")
            .language("vi")
            .isCritical(true)
            .title("🚨 Cảnh báo té ngã!")
            .body("Cảm biến phát hiện {name} vừa té ngã tại {location}. Mở app ngay!")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        templateRepository.save(pushTemplate);
      }
      log.info("Check Notification Templates DONE.");
    };
  }
}

package com.healthcarenow.notification.config;

import com.healthcarenow.notification.model.NotificationTemplate;
import com.healthcarenow.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Seed default notification templates on service startup
 * These templates support template variables like {name}, {value}, {unit}
 */
@Configuration
@Slf4j
public class TemplateSeederConfig {

  @Bean
  public CommandLineRunner seedNotificationTemplates(NotificationTemplateRepository templateRepository) {
    return args -> {
      log.info("[TemplateSeeder] Starting notification template seeding...");
      
      long existingCount = templateRepository.count();
      if (existingCount > 0) {
        log.info("[TemplateSeeder] Templates already exist (count={}), skipping seed", existingCount);
        return;
      }
      
      List<NotificationTemplate> templates = Arrays.asList(
        // WATER_REMINDER - multiple channels & languages
        NotificationTemplate.builder()
          .code("WATER_REMINDER")
          .type("PUSH")
          .language("vi")
          .title("💧 Nhắc nhở uống nước")
          .body("Hôm nay bạn đã uống {current}ml, mục tiêu {goal}ml. Hãy uống {needed}ml nữa!")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Water intake reminder push notification (Vietnamese)")
          .supportedVariables("[\"current\", \"goal\", \"needed\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        NotificationTemplate.builder()
          .code("WATER_REMINDER")
          .type("PUSH")
          .language("en")
          .title("💧 Water Reminder")
          .body("You've consumed {current}ml today, goal is {goal}ml. Drink {needed}ml more!")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Water intake reminder push notification (English)")
          .supportedVariables("[\"current\", \"goal\", \"needed\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // ACTIVITY_COMPLETED
        NotificationTemplate.builder()
          .code("ACTIVITY_COMPLETED")
          .type("PUSH")
          .language("vi")
          .title("🏃 Hoạt động hoàn thành")
          .body("Xin chúc mừng {name}! Bạn vừa hoàn thành {type} với {calories} calories đốt cháy.")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Activity completion celebration (Vietnamese)")
          .supportedVariables("[\"name\", \"type\", \"calories\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        NotificationTemplate.builder()
          .code("ACTIVITY_COMPLETED")
          .type("PUSH")
          .language("en")
          .title("🏃 Activity Completed")
          .body("Great job, {name}! You completed {type} and burned {calories} calories.")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Activity completion celebration (English)")
          .supportedVariables("[\"name\", \"type\", \"calories\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // FALL_DETECTED - CRITICAL
        NotificationTemplate.builder()
          .code("FALL_DETECTED")
          .type("PUSH")
          .language("vi")
          .title("⚠️ SOS - Phát hiện rơi")
          .body("Hệ thống phát hiện bạn rơi tại {location} lúc {time}. Hãy bấm OK nếu bạn ổn. Nếu cần, gọi người thân.")
          .priority("CRITICAL")
          .enabled(true)
          .version(1)
          .description("Fall detection alert (Vietnamese) - HIGH PRIORITY")
          .supportedVariables("[\"location\", \"time\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        NotificationTemplate.builder()
          .code("FALL_DETECTED")
          .type("PUSH")
          .language("en")
          .title("⚠️ SOS - Fall Detected")
          .body("A fall was detected at {location} at {time}. Tap OK if you're fine. Call emergency if needed.")
          .priority("CRITICAL")
          .enabled(true)
          .version(1)
          .description("Fall detection alert (English) - HIGH PRIORITY")
          .supportedVariables("[\"location\", \"time\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // HIGH_HEART_RATE
        NotificationTemplate.builder()
          .code("HIGH_HEART_RATE")
          .type("PUSH")
          .language("vi")
          .title("❤️ Nhịp tim cao")
          .body("Nhịp tim bạn đang ở {bpm} bpm, cao hơn bình thường ({normal}). Hãy nghỉ ngơi một chút.")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("High heart rate alert (Vietnamese)")
          .supportedVariables("[\"bpm\", \"normal\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // LOW_SLEEP_ALERT
        NotificationTemplate.builder()
          .code("LOW_SLEEP_ALERT")
          .type("PUSH")
          .language("vi")
          .title("😴 Cảnh báo ngủ hôm qua")
          .body("Hôm qua bạn ngủ chỉ {hours} giờ, dưới mục tiêu {goal} giờ. Hôm nay hãy cố gắng ngủ đủ.")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Low sleep alert (Vietnamese)")
          .supportedVariables("[\"hours\", \"goal\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // APPOINTMENT_REMINDER
        NotificationTemplate.builder()
          .code("APPOINTMENT_REMINDER")
          .type("PUSH")
          .language("vi")
          .title("📅 Nhắc lịch hẹn")
          .body("Lịch hẹn \"{title}\" sắp diễn ra lúc {time}. Chuẩn bị sẵn sàng!")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Appointment reminder (Vietnamese)")
          .supportedVariables("[\"title\", \"time\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // MEDICATION_TIME
        NotificationTemplate.builder()
          .code("MEDICATION_TIME")
          .type("PUSH")
          .language("vi")
          .title("💊 Lúc uống thuốc")
          .body("Giờ uống {medication_name}. Liều lượng: {dosage}. Bạn đã uống chưa?")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("Medication reminder (Vietnamese)")
          .supportedVariables("[\"medication_name\", \"dosage\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // EMAIL templates (fallback channel)
        NotificationTemplate.builder()
          .code("FALL_DETECTED")
          .type("EMAIL")
          .language("vi")
          .title("SOS: Phát hiện rơi ngay lập tức")
          .body("<h1>⚠️ Cảnh báo rơi từ HealthCareNow</h1>" +
            "<p>Hệ thống của HealthCareNow phát hiện người thân <strong>{name}</strong> có dấu hiệu rơi tại <strong>{location}</strong> vào lúc <strong>{time}</strong>.</p>" +
            "<p>Vui lòng liên lạc với họ ngay để kiểm tra tình trạng.</p>" +
            "<p style='color: red;'><strong>Nếu cần, hãy gọi cứu thương</strong></p>")
          .priority("CRITICAL")
          .enabled(true)
          .version(1)
          .description("Fall detection email alert (Vietnamese) - sent to emergency contacts")
          .supportedVariables("[\"name\", \"location\", \"time\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build()
      );
      
      templateRepository.saveAll(templates);
      log.info("[TemplateSeeder] ✅ Seeded {} notification templates", templates.size());
    };
  }
}

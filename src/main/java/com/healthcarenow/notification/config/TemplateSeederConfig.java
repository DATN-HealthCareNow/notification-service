package com.healthcarenow.notification.config;

import com.healthcarenow.notification.model.NotificationTemplate;
import com.healthcarenow.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.ArrayList;
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

        // LOW_EXERCISE_REMINDER
        NotificationTemplate.builder()
          .code("LOW_EXERCISE_REMINDER")
          .type("PUSH")
          .language("vi")
          .title("Hôm qua bạn ít vận động quá")
          .body("Hôm qua bạn chỉ vận động {exercise_minutes} phút (< {target_minutes}p). Hôm nay cố gắng tập thêm {missing_minutes} phút để cân bằng sức khỏe nhé!")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Low exercise reminder (Vietnamese, playful)")
          .supportedVariables("[\"exercise_minutes\", \"target_minutes\", \"missing_minutes\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),

        NotificationTemplate.builder()
          .code("LOW_EXERCISE_REMINDER")
          .type("PUSH")
          .language("en")
          .title("Yesterday was light, let's crush today")
          .body("You only exercised {exercise_minutes} minutes yesterday (< {target_minutes}m). Let's add {missing_minutes} minutes today.")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Low exercise reminder (English)")
          .supportedVariables("[\"exercise_minutes\", \"target_minutes\", \"missing_minutes\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        // ACTIVITY_REMINDER - 7AM playful reminder
        NotificationTemplate.builder()
          .code("ACTIVITY_REMINDER")
          .type("PUSH")
          .language("vi")
          .title("Duy trì vận động hôm nay nhé! 🏃")
          .body("Hôm qua bạn đã tập {exerciseMinutes} phút rồi. Hôm nay tiếp tục duy trì thói quen vận động nha! 💪")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Daily activity reminder at 7AM for users meeting exercise goal (Vietnamese)")
          .supportedVariables("[\"exerciseMinutes\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
        
        NotificationTemplate.builder()
          .code("ACTIVITY_REMINDER")
          .type("PUSH")
          .language("en")
          .title("Keep your momentum today! 🏃")
          .body("You exercised {exerciseMinutes} minutes yesterday. Keep the healthy streak going today! 💪")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Daily activity reminder at 7AM for users meeting exercise goal (English)")
          .supportedVariables("[\"exerciseMinutes\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),

        // NEW_ARTICLE_PUBLISHED
        NotificationTemplate.builder()
          .code("NEW_ARTICLE_PUBLISHED")
          .type("PUSH")
          .language("vi")
          .title("📰 Bài viết mới: {article_title}")
          .body("Chuyên mục {article_category} vừa có bài mới. Mở app để xem chi tiết.")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Notify users when a new health article is published (Vietnamese)")
          .supportedVariables("[\"article_title\", \"article_category\", \"article_id\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),

        NotificationTemplate.builder()
          .code("NEW_ARTICLE_PUBLISHED")
          .type("PUSH")
          .language("en")
          .title("📰 New article: {article_title}")
          .body("A new {article_category} article is now available. Open the app to read it.")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Notify users when a new health article is published (English)")
          .supportedVariables("[\"article_title\", \"article_category\", \"article_id\"]")
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
          .build(),

        NotificationTemplate.builder()
          .code("PASSWORD_OTP")
          .type("EMAIL")
          .language("vi")
          .title("Ma OTP {purpose} cua ban")
          .body("<h2>Yeu cau {purpose}</h2>" +
            "<p>Ma OTP cua ban la: <strong style='font-size: 22px'>{otp_code}</strong></p>" +
            "<p>Ma co hieu luc trong <strong>{otp_expiry_minutes} phut</strong>.</p>" +
            "<p>Neu ban khong thuc hien thao tac nay, vui long bo qua email.</p>")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("OTP email for change/forgot password flow (Vietnamese)")
          .supportedVariables("[\"purpose\", \"otp_code\", \"otp_expiry_minutes\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build()
      );

      List<NotificationTemplate> missingTemplates = new ArrayList<>();
      for (NotificationTemplate template : templates) {
        boolean exists = templateRepository
            .findByCodeAndTypeAndLanguage(template.getCode(), template.getType(), template.getLanguage())
            .isPresent();
        if (!exists) {
          missingTemplates.add(template);
        }
      }

      if (missingTemplates.isEmpty()) {
        log.info("[TemplateSeeder] All default templates already exist. Nothing to seed.");
        return;
      }

      templateRepository.saveAll(missingTemplates);
      log.info("[TemplateSeeder] ✅ Seeded {} missing notification templates", missingTemplates.size());
    };
  }
}

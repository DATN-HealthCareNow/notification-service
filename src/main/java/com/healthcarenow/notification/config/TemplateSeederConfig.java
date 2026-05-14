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
          .code("EXERCISE_SCHEDULE_REMINDER")
          .type("PUSH")
          .language("vi")
          .title("⏰ Đến giờ tập luyện rồi!")
          .body("Lịch tập luyện: {schedule_title}. Đã đến giờ rồi, hãy bắt đầu ngay để cảm thấy sảng khoái hơn nhé! 💪")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("Specific reminder for a scheduled exercise (Vietnamese)")
          .supportedVariables("[\"schedule_title\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),

        NotificationTemplate.builder()
          .code("EXERCISE_SCHEDULE_REMINDER")
          .type("PUSH")
          .language("en")
          .title("⏰ Time to exercise!")
          .body("It's time for your: {schedule_title}. Let's get moving and feel the energy! 💪")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("Specific reminder for a scheduled exercise (English)")
          .supportedVariables("[\"schedule_title\"]")
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
        
        // MEAL_REMINDER
        NotificationTemplate.builder()
          .code("MEAL_REMINDER")
          .type("PUSH")
          .language("vi")
          .title("🍽️ Đến giờ ăn rồi!")
          .body("Đã đến lúc nạp năng lượng cho {meal_name}. Đừng bỏ bữa bạn nhé!")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Meal reminder push notification (Vietnamese)")
          .supportedVariables("[\"meal_name\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),

        NotificationTemplate.builder()
          .code("MEAL_REMINDER")
          .type("PUSH")
          .language("en")
          .title("🍽️ Meal Time!")
          .body("It's time for your {meal_name}. Don't skip your meal!")
          .priority("NORMAL")
          .enabled(true)
          .version(1)
          .description("Meal reminder push notification (English)")
          .supportedVariables("[\"meal_name\"]")
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
          .body("<div style=\"margin:0;padding:0;background:#f3f6fb;\">" +
            "  <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background:#f3f6fb;padding:24px 12px;\">" +
            "    <tr><td align=\"center\">" +
            "      <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width:560px;background:#ffffff;border-radius:16px;border:1px solid #e5eaf2;overflow:hidden;\">" +
            "        <tr><td style=\"padding:18px 20px;background:#f8fbff;border-bottom:1px solid #eef3fb;\">" +
            "          <span style=\"display:inline-block;width:26px;height:26px;line-height:26px;border-radius:13px;background:#111827;color:#38bdf8;text-align:center;font-size:14px;font-weight:700;font-family:Arial,sans-serif;margin-right:8px;\">H</span>" +
            "          <span style=\"font-family:Arial,sans-serif;font-size:21px;font-weight:700;color:#3b82f6;vertical-align:middle;\">HealthCareNow</span>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:28px 24px 8px 24px;font-family:Arial,sans-serif;\">" +
            "          <div style=\"font-size:34px;line-height:1.2;color:#1f2937;font-weight:700;margin-bottom:10px;\">Hello there,</div>" +
            "          <div style=\"font-size:15px;line-height:1.6;color:#6b7280;margin-bottom:18px;\">Your verification code for HealthCareNow (<strong>{purpose}</strong>) is:</div>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:0 24px;\">" +
            "          <div style=\"border:1px solid #e8edf5;border-radius:10px;background:#ffffff;text-align:center;padding:24px 12px;\">" +
            "            <div style=\"font-family:'Courier New',monospace;font-size:52px;line-height:1.1;letter-spacing:8px;color:#3b82f6;font-weight:700;\">{otp_code}</div>" +
            "          </div>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:14px 24px 8px 24px;font-family:Arial,sans-serif;\">" +
            "          <div style=\"font-size:13px;color:#6b7280;\">&#9432; This code will expire in {otp_expiry_minutes} minutes.</div>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:8px 24px 18px 24px;font-family:Arial,sans-serif;\">" +
            "          <div style=\"font-size:12px;line-height:1.6;color:#94a3b8;\">If you didn&apos;t request this code, please ignore this email or contact our <a href=\"#\" style=\"color:#3b82f6;text-decoration:none;font-weight:600;\">support team</a>.</div>" +
            "        </td></tr>" +
            "      </table>" +
            "    </td></tr>" +
            "  </table>" +
            "</div>")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("OTP email for change/forgot password flow (Vietnamese)")
          .supportedVariables("[\"purpose\", \"otp_code\", \"otp_expiry_minutes\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),
          
        NotificationTemplate.builder()
          .code("REGISTER_OTP")
          .type("EMAIL")
          .language("vi")
          .title("Ma OTP xac nhan dang ky tai khoan")
          .body("<div style=\"margin:0;padding:0;background:#f3f6fb;\">" +
            "  <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background:#f3f6fb;padding:24px 12px;\">" +
            "    <tr><td align=\"center\">" +
            "      <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width:560px;background:#ffffff;border-radius:16px;border:1px solid #e5eaf2;overflow:hidden;\">" +
            "        <tr><td style=\"padding:18px 20px;background:#f8fbff;border-bottom:1px solid #eef3fb;\">" +
            "          <span style=\"display:inline-block;width:26px;height:26px;line-height:26px;border-radius:13px;background:#111827;color:#38bdf8;text-align:center;font-size:14px;font-weight:700;font-family:Arial,sans-serif;margin-right:8px;\">H</span>" +
            "          <span style=\"font-family:Arial,sans-serif;font-size:21px;font-weight:700;color:#3b82f6;vertical-align:middle;\">HealthCareNow</span>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:28px 24px 8px 24px;font-family:Arial,sans-serif;\">" +
            "          <div style=\"font-size:34px;line-height:1.2;color:#1f2937;font-weight:700;margin-bottom:10px;\">Hello there,</div>" +
            "          <div style=\"font-size:15px;line-height:1.6;color:#6b7280;margin-bottom:18px;\">Your verification code for account registration is:</div>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:0 24px;\">" +
            "          <div style=\"border:1px solid #e8edf5;border-radius:10px;background:#ffffff;text-align:center;padding:24px 12px;\">" +
            "            <div style=\"font-family:'Courier New',monospace;font-size:52px;line-height:1.1;letter-spacing:8px;color:#3b82f6;font-weight:700;\">{otp_code}</div>" +
            "          </div>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:14px 24px 8px 24px;font-family:Arial,sans-serif;\">" +
            "          <div style=\"font-size:13px;color:#6b7280;\">&#9432; This code will expire in {otp_expiry_minutes} minutes.</div>" +
            "        </td></tr>" +
            "        <tr><td style=\"padding:8px 24px 18px 24px;font-family:Arial,sans-serif;\">" +
            "          <div style=\"font-size:12px;line-height:1.6;color:#94a3b8;\">If you didn&apos;t request this code, please ignore this email or contact our <a href=\"#\" style=\"color:#3b82f6;text-decoration:none;font-weight:600;\">support team</a>.</div>" +
            "        </td></tr>" +
            "      </table>" +
            "    </td></tr>" +
            "  </table>" +
            "</div>")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("OTP email for account registration (Vietnamese)")
          .supportedVariables("[\"otp_code\", \"otp_expiry_minutes\"]")
          .createdTimeUnix(Instant.now().getEpochSecond())
          .updatedTimeUnix(Instant.now().getEpochSecond())
          .build(),

        NotificationTemplate.builder()
          .code("REGISTER_OTP")
          .type("EMAIL")
          .language("en")
          .title("Your account registration OTP")
          .body("<h2>Confirm your registration</h2>" +
            "<p>Your OTP code is: <strong style='font-size: 22px'>{otp_code}</strong></p>" +
            "<p>This code expires in <strong>{otp_expiry_minutes} minutes</strong>.</p>" +
            "<p>If you didn't request this, please ignore this email.</p>")
          .priority("HIGH")
          .enabled(true)
          .version(1)
          .description("OTP email for account registration (English)")
          .supportedVariables("[\"otp_code\", \"otp_expiry_minutes\"]")
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

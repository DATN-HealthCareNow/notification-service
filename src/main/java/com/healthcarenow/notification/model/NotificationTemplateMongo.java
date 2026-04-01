package com.healthcarenow.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * NotificationTemplate - MongoDB document storing reusable notification templates
 * Can be multilingual and type-specific (PUSH, EMAIL, IN_APP)
 */
@Document(collection = "notification_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
  
  @Id
  private String id;
  
  // Template identification
  private String code;            // e.g., "WATER_REMINDER", "ACTIVITY_COMPLETED"
  private String type;            // PUSH, EMAIL, IN_APP
  private String language;        // vi, en, etc.
  
  // Content with template variables (e.g., {name}, {value}, {unit})
  private String title;           // Title of notification
  private String body;            // Main content (can be HTML for email)
  
  // Configuration
  private String priority;        // LOW, NORMAL, HIGH, CRITICAL
  private Boolean enabled;        // Is this template active?
  private Integer version;        // For versioning of template content
  
  // Usage hints
  private String description;     // Internal note about when/why to use this template
  private String supportedVariables; // JSON array of variables like ["name", "value", "unit"]
  
  // Metadata
  private Long createdTimeUnix;
  private Long updatedTimeUnix;
  
  /**
   * Example templates:
   * 
   * Code: WATER_REMINDER
   * Type: PUSH
   * Language: vi
   * Title: "💧 Nhắc nhở uống nước"
   * Body: "Hôm nay bạn đã uống {current}ml, mục tiêu {goal}ml. Hãy uống {needed}ml nữa!"
   * Variables: ["current", "goal", "needed"]
   * 
   * Code: FALL_DETECTED
   * Type: PUSH
   * Language: vi
   * Priority: CRITICAL
   * Title: "⚠️ SOS - Phát hiện rơi"
   * Body: "Hệ thống phát hiện bạn rơi tại {location} lúc {time}. Gọi cứu thương?"
   * Variables: ["location", "time"]
   */
}

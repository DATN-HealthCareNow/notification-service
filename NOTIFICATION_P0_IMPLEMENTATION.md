# HealthCareNow Notification Service - Implementation Roadmap (P0 Release)

## 🎯 Gói P0: "The Pulse" - Build thông luồng thông báo end-to-end

**Ngày**: April 1, 2026  
**Status**: ✅ Implementation completed

---

## 📋 Những gì đã hoàn thành trong P0

### 1️⃣ **Fix lỗi cấu hình quan trọng** ✅
- ✅ CoreServiceClient port: 8080 → **8081** (đúng với core-service)
- ✅ RabbitMQ routing key: `#` → **`notification.#`** (hẹp hóa chỉ nhận notification event)
- ✅ Internal token: hard-code → **`@Value("${app.internal-token}")`** (move to environment)
- ✅ Environment variables: Thêm `APP_INTERNAL_TOKEN`, `CORE_SERVICE_URL` vào docker-compose.dev.yml, docker-compose.yml

**Files sửa:**
- `notification-service/src/main/java/com/healthcarenow/notification/client/CoreServiceClient.java`
- `notification-service/src/main/java/com/healthcarenow/notification/config/RabbitMQConfig.java`
- `notification-service/src/main/java/com/healthcarenow/notification/service/NotificationResolver.java`
- `core-service/src/main/java/com/healthcarenow/core/controller/InternalUserController.java`
- `notification-service/src/main/resources/application.yml`
- `core-service/src/main/resources/application.yml`
- `devops-service/docker-compose.dev.yml`
- `devops-service/docker-compose.yml`

---

### 2️⃣ **Contract Event chuẩn hóa (v1)** ✅

**File mới:** `devops-service/contracts/events/notification.event.v1.schema.json`

```json
{
  "event_type": "WATER_REMINDER | ACTIVITY_COMPLETED | FALL_DETECTED | HIGH_HEART_RATE | LOW_SLEEP_ALERT | APPOINTMENT_REMINDER | MEDICATION_TIME",
  "priority": "LOW | NORMAL | HIGH | CRITICAL",
  "payload": {
    "user_id": "...",
    "device_token": "ExponentPushToken[...]",  // optional, fallback from core-service
    "email": "user@example.com",               // optional, fallback from core-service
    "title": "💧 Nhắc nhở uống nước",         // template variables: {name}, {value}, {unit}
    "body": "Hôm nay bạn đã uống {current}ml"
    "metadata": { "current": "500", "goal": "2000", "needed": "1500" }
  }
}
```

**Lợi ích:**
- Event versioning cho backward compatibility
- Idempotency tracking via `event_id`
- Distributed tracing qua `correlation_id`

---

### 3️⃣ **MongoDB Models & Repositories** ✅

**Tạo 3 collections:**

#### **notification_logs** - Lưu lịch sử tất cả thông báo đã gửi
```
{
  _id: ObjectId,
  userId: "...",
  templateId: "...",
  eventId: "..." (for idempotency),
  type: "PUSH | EMAIL | IN_APP",
  title, content, recipient,
  status: "PENDING | SENT | FAILED | BOUNCED",
  priority: "LOW | NORMAL | HIGH | CRITICAL",
  isRead: true/false,
  createdAt, sentAt, readAt
}
```

#### **notification_templates** - Mẫu thông báo multilingual
```
{
  _id: ObjectId,
  code: "WATER_REMINDER | ACTIVITY_COMPLETED | FALL_DETECTED | ...",
  type: "PUSH | EMAIL | IN_APP",
  language: "vi | en",
  title: "💧 Nhắc nhở uống nước",
  body: "Hôm nay bạn đã uống {current}ml, mục tiêu {goal}ml",
  enabled: true,
  supportedVariables: ["current", "goal", "needed"]
}
```

#### **notification_preferences** - Settings theo user
```
{
  _id: ObjectId,
  userId: "...",
  allNotificationsEnabled: true,
  preferredLanguage: "vi",
  pushEnabled: true,
  emailEnabled: true,
  enabledEventTypes: {
    "WATER_REMINDER": true,
    "FALL_DETECTED": true,
    "ACTIVITY_COMPLETED": false
  },
  quietHoursStart: "22:00",
  quietHoursEnd: "08:00",
  quietHoursEnabled: false
}
```

**Files tạo:**
- `notification-service/src/main/java/com/healthcarenow/notification/model/NotificationLogMongo.java`
- `notification-service/src/main/java/com/healthcarenow/notification/model/NotificationTemplateMongo.java`
- `notification-service/src/main/java/com/healthcarenow/notification/model/NotificationPreferenceMongo.java`
- `notification-service/src/main/java/com/healthcarenow/notification/repository/NotificationPreferenceRepository.java`
- (NotificationLogRepository và NotificationTemplateRepository đã tồn tại)

---

### 4️⃣ **Template Seeding** ✅

**File:** `notification-service/src/main/java/com/healthcarenow/notification/config/TemplateSeederConfig.java`

Seed 10 templates mẫu khi service khởi động:
- ✅ WATER_REMINDER (Push VI/EN)
- ✅ ACTIVITY_COMPLETED (Push VI/EN)
- ✅ FALL_DETECTED (Push + Email VI, CRITICAL)
- ✅ HIGH_HEART_RATE (Push VI)
- ✅ LOW_SLEEP_ALERT (Push VI)
- ✅ APPOINTMENT_REMINDER (Push VI)
- ✅ MEDICATION_TIME (Push VI)

---

### 5️⃣ **REST API Endpoints** ✅

**Base URL:** `http://localhost/api/v1/notifications` (chạy qua Nginx Gateway)

#### **Notification Log API:**

| Method | Endpoint | Mục đích | Auth |
|--------|----------|---------|------|
| GET | `/notifications` | Lấy danh sách thông báo (paginated) | Bearer + x-user-id |
| GET | `/notifications/unread` | Lấy chỉ thông báo chưa đọc | Bearer + x-user-id |
| GET | `/notifications/unread-count` | Đếm thông báo chưa đọc | Bearer + x-user-id |
| PATCH | `/notifications/{id}/read` | Đánh dấu 1 thông báo là đã đọc | Bearer + x-user-id |
| PATCH | `/notifications/read-all` | Đánh dấu tất cả là đã đọc | Bearer + x-user-id |

#### **Preference Settings API:**

| Method | Endpoint | Mục đích | Auth |
|--------|----------|---------|------|
| GET | `/notifications/preferences` | Lấy cài đặt notification user | Bearer + x-user-id |
| PATCH | `/notifications/preferences` | Cập nhật cài đặt (on/off type) | Bearer + x-user-id |

**Files tạo:**
- `notification-service/src/main/java/com/healthcarenow/notification/controller/NotificationController.java`
- `notification-service/src/main/java/com/healthcarenow/notification/dto/NotificationLogDTO.java`
- `notification-service/src/main/java/com/healthcarenow/notification/dto/NotificationPreferenceDTO.java`
- `notification-service/src/main/java/com/healthcarenow/notification/service/NotificationLogService.java`
- `notification-service/src/main/java/com/healthcarenow/notification/service/NotificationPreferenceService.java`

---

### 6️⃣ **Nginx & Docker Compose Updates** ✅

**Nginx routing:**
```nginx
location ~ ^/api/v1/notifications {
  auth_request /_auth_validate;
  auth_request_set $x_user_id $upstream_http_x_user_id;
  set $notif_upstream notification-service:8084;
  # Forward to notification-service with x-user-id header
}
```

**Docker-compose env vars:**
```yaml
notification-service:
  environment:
    APP_INTERNAL_TOKEN: ${APP_INTERNAL_TOKEN:-hcn-internal-secret-2024}
    CORE_SERVICE_URL: ${CORE_SERVICE_URL:-http://core-service:8081}
```

---

## 🔄 Flow Luồng End-to-End (P0)

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. TRIGGER EVENT (IoT/Core Service)                            │
├─────────────────────────────────────────────────────────────────┤
│ IoT-Service detect: Te ngã, nhịp tim cao, thiếu exercise       │
│ Core-Service trigger: Đến giờ uống nước, lịch hẹn đến          │
│ → Publish event lên RabbitMQ (notification.event routing key)   │
│                                                                 │
│ Event example:                                                  │
│ {                                                               │
│   event_id: "550e8400-e29b-41d4-a716-446655440000",            │
│   event_type: "WATER_REMINDER",                                │
│   priority: "NORMAL",                                          │
│   payload: {                                                    │
│     user_id: "65a1b2c3d4e5f6a7b8c9d001",                       │
│     metadata: { current: "500", goal: "2000", needed: "1500" } │
│   }                                                             │
│ }                                                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. NOTIFICATION SERVICE CONSUMES EVENT                          │
├─────────────────────────────────────────────────────────────────┤
│ @RabbitListener(queues = "notification.queue")                 │
│ → Receive event từ RabbitMQ                                     │
│ → Check idempotency: Redis key event:{event_id}                │
│ → Load template: WATER_REMINDER + language + type (PUSH/EMAIL) │
│ → Resolve contact info:                                        │
│   - Try payload.device_token, payload.email first              │
│   - If missing → call Core-Service /api/v1/internal/users/{id} │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. DISPATCH NOTIFICATION                                        │
├─────────────────────────────────────────────────────────────────┤
│ Channel 1: PUSH (để app closed)                                │
│   → Call Expo Push API with device_token                       │
│   → Response: sent or failed (e.g., DeviceNotRegistered)       │
│   → Save status: SENT / FAILED to notification_log             │
│                                                                 │
│ Channel 2: EMAIL (fallback for critical)                       │
│   → Call SMTP to send HTML email                               │
│   → Save status: SENT / FAILED                                 │
│                                                                 │
│ Channel 3: IN_APP LOG (luôn save)                              │
│   → Save record trong notification_log (status: SENT/FAILED)   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. MOBILE APP READS NOTIFICATION                               │
├─────────────────────────────────────────────────────────────────┤
│ Push channel:                                                   │
│   → User cảm nhận trên status bar (lockscreen)                 │
│   → Tap → open app                                              │
│   → Expo handler trigger                                        │
│                                                                 │
│ In-app channel (log):                                           │
│   → GET http://localhost/api/v1/notifications                  │
│   → Return Page<NotificationLogDTO>                            │
│   → Display in Notification History tab                        │
│                                                                 │
│ Mark read:                                                      │
│   → PATCH /api/v1/notifications/{id}/read                      │
│   → Update isRead=true, readAt=now                            │
│   → Return updated DTO                                          │
│                                                                 │
│ Settings:                                                       │
│   → GET /api/v1/notifications/preferences                      │
│   → PATCH /api/v1/notifications/preferences                    │
│   → User control on/off per event type                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Cách test P0 end-to-end locally

### **Prerequisite:**
```bash
cp devops-service/.env.example devops-service/.env
# Edit .env điền SPRING_MAIL_USERNAME, SPRING_MAIL_PASSWORD cho Expo/SMTP test

cd devops-service
./scripts/start-all.sh
./scripts/init-databases.sh
```

### **Test Flow:**

#### **1. Register & Login mobile app**
```bash
# Register
POST http://localhost/api/v1/auth/register
{
  "email": "test@example.com",
  "password": "password123",
  "full_name": "Test User"
}
# Response: { token: "...", user_id: "..." }

# Save token & userId to SecureStore
```

#### **2. Update device token (cho push)**
```bash
POST http://localhost/api/v1/users/device-token
Header: Authorization: Bearer <token>
{
  "device_token": "ExponentPushToken[xxxxx]"  # or mock token cho test
}
```

#### **3. Publish test event từ IoT-Service**
```bash
# Connect vào iot-service bash shell
docker-compose exec iot-service bash

# Chạy curl để publish event lên RabbitMQ
curl -X POST http://localhost:15672/api/exchanges/healthcare.events/publish \
  -u guest:guest \
  -H "Content-Type: application/json" \
  -d '{
    "routing_key": "notification.water.logged",
    "payload": "{
      \"event_id\": \"550e8400-e29b-41d4-a716-446655440000\",
      \"event_type\": \"WATER_REMINDER\",
      \"event_version\": 1,
      \"timestamp\": \"2026-04-01T12:00:00Z\",
      \"correlation_id\": \"corr-123\",
      \"priority\": \"NORMAL\",
      \"payload\": {
        \"user_id\": \"<your_user_id>\",
        \"metadata\": { \"current\": \"500\", \"goal\": \"2000\", \"needed\": \"1500\" }
      }
    }"
  }'
```

**Hoặc dùng RabbitMQ Management UI:**
- http://localhost:15672 (guest/guest)
- Vào Exchanges → healthcare.events
- Publish message với routing_key = `notification.water.logged`

#### **4. Check Notification History (API)**
```bash
GET http://localhost/api/v1/notifications?page=0&size=20
Header: Authorization: Bearer <token>
Header: x-user-id: <user_id>

# Response:
{
  "content": [
    {
      "id": "...",
      "userId": "...",
      "type": "PUSH",
      "title": "💧 Nhắc nhở uống nước",
      "content": "Hôm nay bạn đã uống 500ml, mục tiêu 2000ml. Hãy uống 1500ml nữa!",
      "status": "SENT",
      "isRead": false,
      "createdAt": "2026-04-01T12:00:00"
    }
  ],
  "totalElements": 1,
  "page": 0,
  "size": 20
}
```

#### **5. Mark as read**
```bash
PATCH http://localhost/api/v1/notifications/{notification_id}/read
Header: Authorization: Bearer <token>
Header: x-user-id: <user_id>

# Response: Updated DTO with isRead=true
```

#### **6. Check/Update preferences**
```bash
GET http://localhost/api/v1/notifications/preferences
Header: Authorization: Bearer <token>
Header: x-user-id: <user_id>

# Response:
{
  "userId": "...",
  "allNotificationsEnabled": true,
  "preferredLanguage": "vi",
  "pushEnabled": true,
  "emailEnabled": true,
  "enabledEventTypes": {
    "WATER_REMINDER": true,
    "FALL_DETECTED": true,
    "ACTIVITY_COMPLETED": true
  }
}

# Disable WATER_REMINDER:
PATCH http://localhost/api/v1/notifications/preferences
{
  "enabledEventTypes": {
    "WATER_REMINDER": false
  }
}
```

#### **7. Check MongoDB records**
```bash
# Connect vào notification_db
docker-compose exec notification_db mongosh

use healthcare_notification
db.notification_logs.find({userId: "..."}).pretty()
db.notification_templates.find({code: "WATER_REMINDER"}).pretty()
db.notification_preferences.find({userId: "..."}).pretty()
```

---

## ✅ Checklist Verification

- [ ] docker-compose up -d chạy thành công (all 9 services up)
- [ ] Core-service health check: http://localhost:8081/actuator/health
- [ ] Notification-service health check: http://localhost:8084/actuator/health
- [ ] MongoDB collections created: notification_logs, notification_templates, notification_preferences
- [ ] Templates seeded: mongosh > db.notification_templates.count() > 10
- [ ] Register & Login test pass
- [ ] Publish event test pass
- [ ] GET /api/v1/notifications return 200
- [ ] PATCH /api/v1/notifications/{id}/read pass
- [ ] GET /api/v1/notifications/preferences return 200
- [ ] PATCH /api/v1/notifications/preferences pass

---

## 📊 Metrics (Prometheus)

Endpoint đã expose (cần verify sau):
```
GET http://localhost:9090/api/v1/targets
```

Metrics để monitor:
```
notification_events_processed_total
notification_send_duration_seconds
notification_delivery_failed_total
rabbitmq_queue_messages_ready
```

---

## 🎯 Kế tiếp - Gói P1 (Sprint Tiếp)

1. **Retry Policy & Dead Letter Queue (DLQ)**
   - Max retry: 3 lần với exponential backoff
   - Failed notification → move to DLX → process separately
   
2. **Real-time Notification (In-app only)**
   - Option A: Polling every 5s (simple, not ideal)
   - Option B: Long-polling (better for battery)
   - Option C: Socket.io (best, need refactor BFF)
   
3. **Reminder Scheduler**
   - Cron job trigger: "Uống nước lúc 9h, 12h, 3h, 6h"
   - Respect quiet hours & user timezone
   
4. **Alert Rule Engine**
   - High heart rate alert rule
   - Low sleep alert rule
   - Custom user alerts

---

## 📝 Documentation

**API Spec**: OpenAPI contract files sẽ được thêm vào `/contracts/rest/notification-api.v1.openapi.yaml`

**Event Schema**: `/contracts/events/notification.event.v1.schema.json`

---

**HealthCareNow Notification Service P0** 🎉  
Ready for QA & Integration Testing  
Author: DevTeam  
Date: April 1, 2026

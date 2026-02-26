package com.healthcarenow.notification.listener;

import com.healthcarenow.notification.config.RabbitMQConfig;
import com.healthcarenow.notification.dto.NotificationEvent;
import com.healthcarenow.notification.service.NotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

  private final NotificationHandler notificationHandler;

  @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
  public void receiveMessage(NotificationEvent event) {
    log.info("Received message from RabbitMQ: {}", event);
    try {
      notificationHandler.processEvent(event);
    } catch (Exception e) {
      log.error("Error processing message, will be routed to DLX: {}", e.getMessage());
      throw e; // This will route message to DLX for retrying
    }
  }
}

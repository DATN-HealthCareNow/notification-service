package com.healthcarenow.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "healthcare.events";

  public static final String NOTIFICATION_QUEUE = "notification.queue";
  public static final String NOTIFICATION_DLQ = "notification.dlq";

  public static final String DEAD_LETTER_EXCHANGE = "healthcare.dlx";
  public static final String ROUTING_KEY_NOTIFICATION = "#"; // all messages to notification exchange

  @Bean
  public TopicExchange healthcareExchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(DEAD_LETTER_EXCHANGE);
  }

  @Bean
  public Queue notificationDlq() {
    return QueueBuilder.durable(NOTIFICATION_DLQ).build();
  }

  @Bean
  public Queue notificationQueue() {
    Map<String, Object> args = new HashMap<>();
    args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
    args.put("x-dead-letter-routing-key", NOTIFICATION_DLQ);
    return QueueBuilder.durable(NOTIFICATION_QUEUE).withArguments(args).build();
  }

  @Bean
  public Binding bindingNotificationQueue(Queue notificationQueue, TopicExchange healthcareExchange) {
    return BindingBuilder.bind(notificationQueue).to(healthcareExchange).with(ROUTING_KEY_NOTIFICATION);
  }

  @Bean
  public Binding bindingDlq(Queue notificationDlq, DirectExchange deadLetterExchange) {
    return BindingBuilder.bind(notificationDlq).to(deadLetterExchange).with(NOTIFICATION_DLQ);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}

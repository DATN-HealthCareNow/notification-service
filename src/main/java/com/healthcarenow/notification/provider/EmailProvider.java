package com.healthcarenow.notification.provider;

import com.healthcarenow.notification.model.NotificationLog;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProvider {

  private final JavaMailSender javaMailSender;

  public boolean sendEmail(NotificationLog notificationLog) {
    log.info("Sending Email to {}", notificationLog.getRecipient());

    try {
      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(notificationLog.getRecipient());
      helper.setSubject(notificationLog.getTitle());
      helper.setText(notificationLog.getContent(), true); // true for HTML

      javaMailSender.send(message);
      log.info("Email sent successfully to {}", notificationLog.getRecipient());
      return true;
    } catch (Exception e) {
      log.error("Failed to send email to {}", notificationLog.getRecipient(), e);
      notificationLog.setProviderResponse(e.getMessage());
      return false;
    }
  }
}

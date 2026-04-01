package com.healthcarenow.notification.client;

import com.healthcarenow.notification.dto.UserContactResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "core-service", url = "${CORE_SERVICE_URL:http://core-service:8081}")
public interface CoreServiceClient {

  @GetMapping("/api/v1/internal/users/{userId}/contact")
  UserContactResponse getContactInfo(
      @RequestHeader("X-Internal-Token") String internalToken,
      @PathVariable("userId") String userId);

  @DeleteMapping("/api/v1/internal/users/{userId}/device-token")
  void removeDeviceToken(
      @RequestHeader("X-Internal-Token") String internalToken,
      @PathVariable("userId") String userId);
}

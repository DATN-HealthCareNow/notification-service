package com.healthcarenow.notification.client;

import com.healthcarenow.notification.dto.ExerciseMetricsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "iot-service", url = "${IOT_SERVICE_URL:http://iot-service:8082}")
public interface IotServiceClient {

  @GetMapping("/api/v1/internal/exercise-metrics/{userId}")
  ExerciseMetricsResponse getExerciseMetrics(
      @RequestHeader("X-Internal-Token") String internalToken,
      @PathVariable("userId") String userId,
      @RequestParam("date") String date);
}

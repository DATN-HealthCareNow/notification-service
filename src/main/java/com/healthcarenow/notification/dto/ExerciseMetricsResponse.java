package com.healthcarenow.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseMetricsResponse {
  private String userId;
  private String dateString;
  private Integer exerciseMinutes;
  private Boolean belowTarget;
}

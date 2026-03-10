package com.servicehub.dto;

import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class SlaPolicyDto {
  private Long id;

  private RequestCategory category;

  private Priority priority;

  private Double responseTimeHours;

  private Double resolutionTimeHours;

}

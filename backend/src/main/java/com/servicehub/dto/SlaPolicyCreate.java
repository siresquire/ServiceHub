package com.servicehub.dto;

import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class SlaPolicyCreate {
  @NotNull
  private RequestCategory category;

  @NotNull
  private Priority priority;

  @NotNull
  @PositiveOrZero
  private Double responseTimeHours;

  @PositiveOrZero
  @NotNull
  private Double resolutionTimeHours;

}

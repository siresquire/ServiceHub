package com.servicehub.dto;

import com.servicehub.model.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class StatusUpdateRequest {
    @NotNull
    private RequestStatus newStatus;
    private String comment;
}

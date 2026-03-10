package com.servicehub.service;

import com.servicehub.dto.SlaPolicyCreate;
import com.servicehub.exception.BadRequestException;
import com.servicehub.model.SlaPolicy;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.repository.SlaPolicyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaPolicyServiceTest {

  @Mock
  private SlaPolicyRepository slaPolicyRepository;

  @InjectMocks
  private SlaPolicyService slaPolicyService;

  @Test
  @DisplayName("Should create SLA Policy successfully")
  void shouldCreateSlaPolicy() {

    SlaPolicyCreate dto = SlaPolicyCreate.builder()
            .priority(Priority.MEDIUM)
            .category(RequestCategory.FACILITIES)
            .responseTimeHours(2D)
            .resolutionTimeHours(8D)
            .build();

    SlaPolicy saved = SlaPolicy.builder()
            .id(1L)
            .priority(Priority.MEDIUM)
            .category(RequestCategory.FACILITIES)
            .responseTimeHours(2D)
            .resolutionTimeHours(8D)
            .build();

    when(slaPolicyRepository.findByPriorityAndCategory(Priority.MEDIUM, RequestCategory.FACILITIES))
            .thenReturn(Optional.empty());

    when(slaPolicyRepository.save(any(SlaPolicy.class))).thenReturn(saved);

    var result = slaPolicyService.create(dto);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(1L, result.getId());
    Assertions.assertEquals(2D, result.getResponseTimeHours());

    verify(slaPolicyRepository).save(any(SlaPolicy.class));
  }

  @Test
  @DisplayName("Should return SLA Policy by ID")
  void shouldReturnSlaPolicyById() {

    SlaPolicy sla = SlaPolicy.builder()
            .id(1L)
            .priority(Priority.MEDIUM)
            .category(RequestCategory.FACILITIES)
            .responseTimeHours(2D)
            .resolutionTimeHours(8D)
            .build();

    when(slaPolicyRepository.findById(1L)).thenReturn(Optional.of(sla));

    var result = slaPolicyService.getById(1L);

    Assertions.assertEquals(1L, result.getId());
    Assertions.assertEquals(2, result.getResponseTimeHours());
  }

  @Test
  @DisplayName("Should delete SLA policy when policy exists")
  void shouldDeleteSlaPolicy() {

    when(slaPolicyRepository.existsById(1L)).thenReturn(true);

    slaPolicyService.delete(1L);

    verify(slaPolicyRepository).deleteById(1L);
  }

  @Test()
  @DisplayName("Should throw BadRequestException when deleting non existing SLA policy")
  void shouldThrowExceptionWhenDeletingNonExistingPolicy() {

    when(slaPolicyRepository.existsById(1L)).thenReturn(false);

    Assertions.assertThrows(BadRequestException.class,
            () -> slaPolicyService.delete(1L));
  }

}

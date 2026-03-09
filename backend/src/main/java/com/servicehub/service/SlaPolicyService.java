package com.servicehub.service;

import com.servicehub.dto.SlaPolicyCreate;
import com.servicehub.dto.SlaPolicyDto;
import com.servicehub.dto.SlaPolicyUpdate;
import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.NotFoundException;
import com.servicehub.model.SlaPolicy;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.repository.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlaPolicyService {

  private final SlaPolicyRepository slaPolicyRepository;

  public SlaPolicyDto create(SlaPolicyCreate dto) {
    var existing = this.slaPolicyRepository.findByPriorityAndCategory(dto.getPriority(), dto.getCategory());

    if (existing.isPresent()) {
      throw new BadRequestException("SLA Policy with same priority and category already exists");
    }

    var sla = slaPolicyRepository.save(
        SlaPolicy.builder()
            .category(dto.getCategory())
            .priority(dto.getPriority())
            .responseTimeHours(dto.getResponseTimeHours())
            .resolutionTimeHours(dto.getResolutionTimeHours())
            .build()
    );

    return SlaPolicyDto.builder()
        .id(sla.getId())
        .category(sla.getCategory())
        .priority(sla.getPriority())
        .responseTimeHours(sla.getResponseTimeHours())
        .resolutionTimeHours(sla.getResolutionTimeHours())
        .build();
  }
  public SlaPolicyDto getById(Long id) {
    return slaPolicyRepository.findById(id)
        .map(sla -> SlaPolicyDto.builder()
            .id(sla.getId())
            .category(sla.getCategory())
            .priority(sla.getPriority())
            .responseTimeHours(sla.getResponseTimeHours())
            .resolutionTimeHours(sla.getResolutionTimeHours())
            .build())
        .orElseThrow(() -> new NotFoundException("SLA Policy not found"));
  }

  public Page<SlaPolicyDto> getAll(Pageable page) {
    return this.slaPolicyRepository.findAll(page)
        .map(sla -> SlaPolicyDto.builder()
            .id(sla.getId())
            .category(sla.getCategory())
            .priority(sla.getPriority())
            .responseTimeHours(sla.getResponseTimeHours())
            .resolutionTimeHours(sla.getResolutionTimeHours())
            .build());
  }

  public List<SlaPolicyDto> getByCategory(RequestCategory category) {
    return this.slaPolicyRepository.findAllByCategory(category)
        .stream()
        .map(sla -> SlaPolicyDto.builder()
            .id(sla.getId())
            .category(sla.getCategory())
            .priority(sla.getPriority())
            .responseTimeHours(sla.getResponseTimeHours())
            .resolutionTimeHours(sla.getResolutionTimeHours())
            .build())
        .toList();
  }

  public SlaPolicyDto update(Long id, SlaPolicyUpdate dto) {
    return slaPolicyRepository.findById(id)
            .map(sla -> {
              sla.setResponseTimeHours(dto.getResponseTimeHours());
              sla.setResolutionTimeHours(dto.getResolutionTimeHours());
              return slaPolicyRepository.save(sla);
            })
            .map(sla -> SlaPolicyDto.builder()
                    .id(sla.getId())
                    .category(sla.getCategory())
                    .priority(sla.getPriority())
                    .responseTimeHours(sla.getResponseTimeHours())
                    .resolutionTimeHours(sla.getResolutionTimeHours())
                    .build())
            .orElseThrow(() -> new BadRequestException("SLA Policy not found"));
  }

  public void delete(Long id) {
    if (!slaPolicyRepository.existsById(id)) {
      throw new BadRequestException("SLA Policy not found");
    }
    slaPolicyRepository.deleteById(id);
  }

}

package com.servicehub.service;

import com.servicehub.dto.SlaPolicyCreate;
import com.servicehub.dto.SlaPolicyDto;
import com.servicehub.dto.SlaPolicyUpdate;
import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.NotFoundException;
import com.servicehub.model.SlaPolicy;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.repository.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
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

  @CacheEvict(value = "slaResponseDeadlines", allEntries = true)
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

  @CacheEvict(value = "slaResponseDeadlines", allEntries = true)
  public void delete(Long id) {
    if (!slaPolicyRepository.existsById(id)) {
      throw new BadRequestException("SLA Policy not found");
    }
    slaPolicyRepository.deleteById(id);
  }

  /**
   * Return the SLA deadline for response based on the category and priority by fetching the corresponding SLA policy and adding the response time to current time.
   * Uses caching to optimize performance for frequently accessed policies.
   * @param category Request category to determine the SLA policy
   * @param priority Request priority to determine the SLA policy
   * @return LocalDateTime representing the SLA deadline for response, calculated as current time plus the response time defined in the SLA policy for the given category and priority
    * @throws NotFoundException if no SLA policy is found for the given category and priority
   */
  @Cacheable(value = "slaResponseDeadlines", key = "#category.toString() + '-' + #priority.toString()", unless = "#result == null")
  public LocalDateTime getResponseSlaDeadline(RequestCategory category, Priority priority) {
    var policy = slaPolicyRepository.findByPriorityAndCategory(priority, category)
        .orElseThrow(() -> new NotFoundException("SLA Policy not found for category " + category + " and priority " + priority));

    // Convert hours (in double) to seconds for Duration
    long seconds = Math.round(policy.getResponseTimeHours() * 3600);

    // Use duration to add the SLA time to current time for accurate handling of fractional hours
    return LocalDateTime.now().plus(Duration.ofSeconds(seconds));
  }

  /**
   * Return the SLA deadline for resolution based on the category and priority by fetching the corresponding SLA policy and adding the resolution time to current time.
   * Uses caching to optimize performance for frequently accessed policies.
   * @param category Request category to determine the SLA policy
   * @param priority Request priority to determine the SLA policy
   * @return LocalDateTime representing the SLA deadline for resolution, calculated as current time plus the resolution time defined in the SLA policy for the given category and priority
   * @throws NotFoundException if no SLA policy is found for the given category and priority
   */
  @Cacheable(value = "slaResolutionDeadlines", key = "#category.toString() + '-' + #priority.toString()", unless = "#result == null")
  public LocalDateTime getResolutionSlaDeadline(RequestCategory category, Priority priority) {
    var policy = slaPolicyRepository.findByPriorityAndCategory(priority, category)
        .orElseThrow(() -> new NotFoundException("SLA Policy not found for category " + category + " and priority " + priority));

    // Convert hours (in double) to seconds for Duration
    long seconds = Math.round(policy.getResolutionTimeHours() * 3600);

    // Use duration to add the SLA time to current time for accurate handling of fractional hours
    return LocalDateTime.now().plus(Duration.ofSeconds(seconds));
  }

}

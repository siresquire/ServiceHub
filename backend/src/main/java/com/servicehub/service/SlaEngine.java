package com.servicehub.service;

import com.servicehub.model.ServiceRequest;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestStatus;
import com.servicehub.repository.ServiceRequestRepository;
import com.servicehub.repository.SlaPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SlaEngine is responsible for tracking SLA deadlines of service requests and escalating their priority if the SLA is breached.
 * It runs as a scheduled task every minute, fetching all SUBMITTED requests in batches and checking if their SLA deadline has passed.
 * If a request has breached its SLA, it escalates the priority and marks the request as SLA breached.
 */

@Service
@RequiredArgsConstructor
public class SlaEngine {

  private final ServiceRequestRepository serviceRequestRepository;
  private final SlaPolicyService slaPolicyService;
  private static final Logger log = LoggerFactory.getLogger(SlaEngine.class);

  /**
   * Scheduled task to run every minute to check for SLA breaches and escalate priority of affected service requests.
   * It fetches all SUBMITTED requests in batches and checks if their SLA deadline has passed.
   * If breached, it escalates the priority and marks the request as SLA breached.
   */
  @Scheduled(fixedRate = 60000)
  public void trackResponseSla() {
    int page = 0;
    int size = 200;
    
    Page<ServiceRequest> requests = serviceRequestRepository.findAllByStatus(RequestStatus.SUBMITTED,  PageRequest.of(page, size));
    log.info("Running SLA Engine to track response SLAs. Found {} SUBMITTED requests to check for SLA breaches.", requests.getTotalElements());
    do {
      List<ServiceRequest> requestList = requests.getContent();
      for (ServiceRequest req : requestList) {
        if (req.getSlaDeadline().isBefore(java.time.LocalDateTime.now())) {
          Priority nextPriority = this.getNextPriority(req.getPriority());
          log.info("SLA breached for request id {}. Escalating priority from {} to {}", req.getId(), req.getPriority(), nextPriority);
          req.setPriority(nextPriority);
          req.setSlaBreached(true);
        }
      }
      serviceRequestRepository.saveAll(requestList);

      page++;
      requests = serviceRequestRepository.findAllByStatus(RequestStatus.SUBMITTED,  PageRequest.of(page, size));

    }while (requests.hasNext());

  }

  @Scheduled(fixedRate = 60000)
  public void trackResolutionSla() {
    int page = 0;
    int size = 200;

    Page<ServiceRequest> requests = serviceRequestRepository.findByStatusOrStatus(RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS,  PageRequest.of(page, size));
    log.info("Running SLA Engine to track resolution SLAs. Found {} ASSIGNED/IN_PROGRESS requests to check for SLA breaches.", requests.getTotalElements());
    do {
      List<ServiceRequest> requestList = requests.getContent();
      for (ServiceRequest req : requestList) {
        if (req.getSlaDeadline().isBefore(java.time.LocalDateTime.now())) {
         Priority nextPriority = this.getNextPriority(req.getPriority());
          log.info("Resolution SLA breached for request id {}. Escalating priority from {} to {}", req.getId(), req.getPriority(), nextPriority);
          req.setPriority(nextPriority);
          req.setSlaBreached(true);
        }
      }
      serviceRequestRepository.saveAll(requestList);

      page++;
      requests = serviceRequestRepository.findAllByStatus(RequestStatus.ASSIGNED,  PageRequest.of(page, size));

    }while (requests.hasNext());
  }

  private Priority getNextPriority(Priority current) {
    return switch (current) {
      case LOW -> Priority.MEDIUM;
      case MEDIUM -> Priority.HIGH;
      case HIGH -> Priority.HIGH; // No escalation beyond HIGH
      case CRITICAL -> Priority.CRITICAL;  // Alert Admin by email
    };
  }






}

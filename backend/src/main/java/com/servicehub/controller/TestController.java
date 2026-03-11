package com.servicehub.controller;

import com.servicehub.dto.ServiceRequestResponse;
import com.servicehub.model.ServiceRequest;
import com.servicehub.model.enums.RequestStatus;
import com.servicehub.repository.ServiceRequestRepository;
import com.servicehub.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
  private final EmailService emailService;
  private final ServiceRequestRepository serviceRequestRepository;

  @GetMapping
  public void testEmail() {
    ServiceRequest serviceRequest = serviceRequestRepository.findById(1L).orElse(new ServiceRequest());
    if (serviceRequest == null) {
      System.out.println("Service request not found");
    }
    emailService.sendSlaNotification(RequestStatus.OPEN, ServiceRequestResponse.toResponse(serviceRequest) );
  }

}

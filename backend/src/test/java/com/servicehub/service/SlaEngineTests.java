package com.servicehub.service;

import com.servicehub.model.ServiceRequest;
import com.servicehub.model.User;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.model.enums.RequestStatus;
import com.servicehub.model.enums.Role;
import com.servicehub.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaEngineTests {

  @Mock
  private EmailService emailService;

  @Mock
  private ServiceRequestRepository serviceRequestRepository;

  @Mock
  private SlaPolicyService slaPolicyService;

  @InjectMocks
  private SlaEngine slaEngine;

  private final User requester = User.builder().id(99L).fullName("Test User").role(Role.USER).build();

  private ServiceRequest buildRequest(Long id, RequestStatus status, Priority priority,
                                      LocalDateTime responseSla, LocalDateTime resolutionSla,
                                      boolean slaBreached) {
    return ServiceRequest.builder()
            .id(id)
            .title("Request " + id)
            .status(status)
            .priority(priority)
            .category(RequestCategory.IT_SUPPORT)
            .requester(requester)
            .responseSlaDeadline(responseSla)
            .resolutionSlaDeadline(resolutionSla)
            .slaBreached(slaBreached)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  // ── trackResponseSla ──────────────────────────────────────────────────────

  @Test
  @DisplayName("Should escalate priority and mark SLA as breached for OPEN requests past their response SLA deadline")
  void shouldEscalatePriorityAndMarkSlaBreachedForOpenRequestsPastDeadline() {
    ServiceRequest req = buildRequest(1L, RequestStatus.OPEN, Priority.LOW,
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(24),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(4));
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(24));

    slaEngine.trackResponseSla();

    assertThat(req.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(req.getSlaBreached()).isTrue();
    verify(emailService).sendSlaNotification(eq(RequestStatus.OPEN), any());
    verify(serviceRequestRepository, times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("Should not send SLA email notification if the request was already marked as SLA breached before")
  void shouldNotSendEmailIfRequestWasAlreadySlaBreached() {
    ServiceRequest req = buildRequest(2L, RequestStatus.OPEN, Priority.MEDIUM,
            LocalDateTime.now().minusMinutes(30),
            LocalDateTime.now().plusHours(12),
            true); // already breached

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(8));

    slaEngine.trackResponseSla();

    verify(emailService, never()).sendSlaNotification(any(), any());
  }

  @Test
  @DisplayName("Should not escalate priority or mark SLA breached for OPEN requests still within their response SLA deadline")
  void shouldSkipRequestsWithResponseSlaDeadlineInTheFuture() {
    ServiceRequest req = buildRequest(3L, RequestStatus.OPEN, Priority.LOW,
            LocalDateTime.now().plusHours(2), // SLA still in the future
            LocalDateTime.now().plusHours(48),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);

    slaEngine.trackResponseSla();

    assertThat(req.getPriority()).isEqualTo(Priority.LOW);
    assertThat(req.getSlaBreached()).isFalse();
    verify(emailService, never()).sendSlaNotification(any(), any());
  }

  @Test
  @DisplayName("Should escalate MEDIUM priority request to HIGH when response SLA is breached")
  void shouldEscalateMediumPriorityToHighOnResponseSlaBreach() {
    ServiceRequest req = buildRequest(4L, RequestStatus.OPEN, Priority.MEDIUM,
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusHours(10),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(1));
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(6));

    slaEngine.trackResponseSla();

    assertThat(req.getPriority()).isEqualTo(Priority.HIGH);
  }

  @Test
  @DisplayName("Should escalate HIGH priority request to CRITICAL when response SLA is breached")
  void shouldEscalateHighPriorityToCriticalOnResponseSlaBreach() {
    ServiceRequest req = buildRequest(5L, RequestStatus.OPEN, Priority.HIGH,
            LocalDateTime.now().minusSeconds(1),
            LocalDateTime.now().plusHours(4),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(1));
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));

    slaEngine.trackResponseSla();

    assertThat(req.getPriority()).isEqualTo(Priority.CRITICAL);
  }

  @Test
  @DisplayName("Should keep CRITICAL priority at CRITICAL and mark SLA breached when response SLA is breached again")
  void shouldKeepCriticalPriorityAtCriticalOnResponseSlaBreach() {
    ServiceRequest req = buildRequest(6L, RequestStatus.OPEN, Priority.CRITICAL,
            LocalDateTime.now().minusSeconds(1),
            LocalDateTime.now().plusHours(1),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusMinutes(30));
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(1));

    slaEngine.trackResponseSla();

    assertThat(req.getPriority()).isEqualTo(Priority.CRITICAL);
    assertThat(req.getSlaBreached()).isTrue();
  }

  @Test
  @DisplayName("Should process the first page of OPEN requests and save all results when checking response SLA breaches")
  void shouldProcessMultiplePagesOfOpenRequestsForResponseSla() {
    ServiceRequest req1 = buildRequest(7L, RequestStatus.OPEN, Priority.LOW,
            LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(24), false);
    ServiceRequest req2 = buildRequest(8L, RequestStatus.OPEN, Priority.LOW,
            LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(20), false);

    Page<ServiceRequest> page0 = new PageImpl<>(List.of(req1, req2), PageRequest.of(0, 200), 2);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 2);

    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(0, 200))))
            .thenReturn(page0);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.OPEN), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(4));
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(24));

    slaEngine.trackResponseSla();

    assertThat(req1.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(req2.getPriority()).isEqualTo(Priority.MEDIUM);
    verify(serviceRequestRepository, times(1)).saveAll(anyList());
  }

  // ── trackResolutionSla ────────────────────────────────────────────────────

  @Test
  @DisplayName("Should escalate priority and mark SLA breached for ASSIGNED or IN_PROGRESS requests past their resolution SLA deadline")
  void shouldEscalatePriorityAndMarkSlaBreachedForRequestsPastResolutionDeadline() {
    ServiceRequest req = buildRequest(10L, RequestStatus.ASSIGNED, Priority.LOW,
            LocalDateTime.now().plusHours(4),
            LocalDateTime.now().minusHours(1), // resolution SLA already passed
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findByStatusOrStatus(eq(RequestStatus.ASSIGNED), eq(RequestStatus.IN_PROGRESS), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.ASSIGNED), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(4));
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));

    slaEngine.trackResolutionSla();

    assertThat(req.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(req.getSlaBreached()).isTrue();
    verify(emailService).sendSlaNotification(eq(RequestStatus.ASSIGNED), any());
  }

  @Test
  @DisplayName("Should not send SLA email notification for resolution breach if request was already marked as SLA breached")
  void shouldNotSendEmailIfResolutionSlaAlreadyBreached() {
    ServiceRequest req = buildRequest(11L, RequestStatus.IN_PROGRESS, Priority.HIGH,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().minusMinutes(30),
            true); // already breached

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findByStatusOrStatus(eq(RequestStatus.ASSIGNED), eq(RequestStatus.IN_PROGRESS), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.ASSIGNED), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(1));

    slaEngine.trackResolutionSla();

    verify(emailService, never()).sendSlaNotification(any(), any());
    assertThat(req.getSlaBreached()).isTrue();
  }

  @Test
  @DisplayName("Should not escalate priority for requests whose resolution SLA deadline has not yet passed")
  void shouldSkipRequestsWithResolutionSlaDeadlineInTheFuture() {
    ServiceRequest req = buildRequest(12L, RequestStatus.ASSIGNED, Priority.LOW,
            LocalDateTime.now().plusHours(2),
            LocalDateTime.now().plusHours(10), // SLA still in the future
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findByStatusOrStatus(eq(RequestStatus.ASSIGNED), eq(RequestStatus.IN_PROGRESS), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.ASSIGNED), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);

    slaEngine.trackResolutionSla();

    assertThat(req.getPriority()).isEqualTo(Priority.LOW);
    assertThat(req.getSlaBreached()).isFalse();
    verify(emailService, never()).sendSlaNotification(any(), any());
  }

  @Test
  @DisplayName("Should escalate MEDIUM priority to HIGH when resolution SLA is breached")
  void shouldEscalateMediumPriorityToHighOnResolutionSlaBreach() {
    ServiceRequest req = buildRequest(13L, RequestStatus.IN_PROGRESS, Priority.MEDIUM,
            LocalDateTime.now().plusHours(4),
            LocalDateTime.now().minusMinutes(1),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findByStatusOrStatus(eq(RequestStatus.ASSIGNED), eq(RequestStatus.IN_PROGRESS), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.ASSIGNED), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(4));
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));

    slaEngine.trackResolutionSla();

    assertThat(req.getPriority()).isEqualTo(Priority.HIGH);
  }

  @Test
  @DisplayName("Should escalate HIGH priority to CRITICAL when resolution SLA is breached")
  void shouldEscalateHighPriorityToCriticalOnResolutionSlaBreach() {
    ServiceRequest req = buildRequest(14L, RequestStatus.ASSIGNED, Priority.HIGH,
            LocalDateTime.now().plusHours(2),
            LocalDateTime.now().minusSeconds(1),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findByStatusOrStatus(eq(RequestStatus.ASSIGNED), eq(RequestStatus.IN_PROGRESS), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.ASSIGNED), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(1));

    slaEngine.trackResolutionSla();

    assertThat(req.getPriority()).isEqualTo(Priority.CRITICAL);
  }

  @Test
  @DisplayName("Should keep CRITICAL priority at CRITICAL when resolution SLA is breached")
  void shouldKeepCriticalPriorityAtCriticalOnResolutionSlaBreach() {
    ServiceRequest req = buildRequest(15L, RequestStatus.IN_PROGRESS, Priority.CRITICAL,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().minusSeconds(1),
            false);

    Page<ServiceRequest> firstPage = new PageImpl<>(List.of(req), PageRequest.of(0, 200), 1);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 1);

    when(serviceRequestRepository.findByStatusOrStatus(eq(RequestStatus.ASSIGNED), eq(RequestStatus.IN_PROGRESS), eq(PageRequest.of(0, 200))))
            .thenReturn(firstPage);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.ASSIGNED), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(1));
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusMinutes(30));

    slaEngine.trackResolutionSla();

    assertThat(req.getPriority()).isEqualTo(Priority.CRITICAL);
    assertThat(req.getSlaBreached()).isTrue();
  }

  @Test
  @DisplayName("Should process all requests in a page and save results when checking resolution SLA breaches")
  void shouldProcessMultiplePagesForResolutionSla() {
    ServiceRequest req1 = buildRequest(16L, RequestStatus.ASSIGNED, Priority.LOW,
            LocalDateTime.now().plusHours(4), LocalDateTime.now().minusHours(1), false);
    ServiceRequest req2 = buildRequest(17L, RequestStatus.IN_PROGRESS, Priority.MEDIUM,
            LocalDateTime.now().plusHours(2), LocalDateTime.now().minusHours(2), false);

    Page<ServiceRequest> page0 = new PageImpl<>(List.of(req1, req2), PageRequest.of(0, 200), 2);
    Page<ServiceRequest> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 200), 2);

    when(serviceRequestRepository.findByStatusOrStatus(eq(RequestStatus.ASSIGNED), eq(RequestStatus.IN_PROGRESS), eq(PageRequest.of(0, 200))))
            .thenReturn(page0);
    when(serviceRequestRepository.findAllByStatus(eq(RequestStatus.ASSIGNED), eq(PageRequest.of(1, 200))))
            .thenReturn(emptyPage);
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(4));
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));

    slaEngine.trackResolutionSla();

    assertThat(req1.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(req2.getPriority()).isEqualTo(Priority.HIGH);
    verify(serviceRequestRepository, times(1)).saveAll(anyList());
  }
}
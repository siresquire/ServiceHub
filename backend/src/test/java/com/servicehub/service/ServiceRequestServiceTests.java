package com.servicehub.service;

import com.servicehub.dto.ServiceRequestDto;
import com.servicehub.dto.ServiceRequestResponse;
import com.servicehub.dto.StatusUpdateRequest;
import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.NotFoundException;
import com.servicehub.model.Department;
import com.servicehub.model.ServiceRequest;
import com.servicehub.model.User;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import com.servicehub.model.enums.RequestStatus;
import com.servicehub.model.enums.Role;
import com.servicehub.repository.DepartmentRepository;
import com.servicehub.repository.ServiceRequestRepository;
import com.servicehub.repository.UserRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTests {

  @Mock
  private ServiceRequestRepository requestRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private DepartmentRepository departmentRepository;

  @Mock
  private SlaPolicyService slaPolicyService;

  @InjectMocks
  private ServiceRequestService serviceRequestService;

  private User adminUser;
  private User agentUser;
  private User regularUser;
  private ServiceRequest openRequest;
  private ServiceRequest assignedRequest;
  private ServiceRequest inProgressRequest;
  private ServiceRequest resolvedRequest;
  private ServiceRequest closedRequest;

  @BeforeEach
  void setUp() {
    adminUser = User.builder().id(1L).role(Role.ADMIN).build();
    agentUser = User.builder().id(2L).role(Role.AGENT).build();
    regularUser = User.builder().id(3L).role(Role.USER).build();

    openRequest = ServiceRequest.builder()
            .id(10L)
            .title("Open Request")
            .status(RequestStatus.OPEN)
            .priority(Priority.MEDIUM)
            .category(RequestCategory.IT_SUPPORT)
            .requester(regularUser)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    assignedRequest = ServiceRequest.builder()
            .id(11L)
            .title("Assigned Request")
            .status(RequestStatus.ASSIGNED)
            .priority(Priority.HIGH)
            .category(RequestCategory.IT_SUPPORT)
            .requester(regularUser)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    inProgressRequest = ServiceRequest.builder()
            .id(12L)
            .title("In Progress Request")
            .status(RequestStatus.IN_PROGRESS)
            .priority(Priority.LOW)
            .category(RequestCategory.HR_REQUEST)
            .requester(regularUser)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    resolvedRequest = ServiceRequest.builder()
            .id(13L)
            .title("Resolved Request")
            .status(RequestStatus.RESOLVED)
            .priority(Priority.LOW)
            .category(RequestCategory.HR_REQUEST)
            .requester(regularUser)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    closedRequest = ServiceRequest.builder()
            .id(14L)
            .title("Closed Request")
            .status(RequestStatus.CLOSED)
            .priority(Priority.LOW)
            .category(RequestCategory.HR_REQUEST)
            .requester(regularUser)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  // ── getAllRequests (paginated) ─────────────────────────────────────────────

  @Test
  @DisplayName("Should return paginated list of all service requests ordered by creation date")
  void shouldReturnPaginatedServiceRequests() {
    Page<ServiceRequest> page = new PageImpl<>(List.of(openRequest));
    when(requestRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(page);

    Page<ServiceRequestResponse> result = serviceRequestService.getAllRequests(0, 10);

    assertThat(result.getContent()).hasSize(1);
    verify(requestRepository).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
  }

  // ── getRequestById ────────────────────────────────────────────────────────

  @Test
  @DisplayName("Should return service request response when request exists")
  void shouldReturnServiceRequestResponseWhenFound() {
    when(requestRepository.findById(10L)).thenReturn(Optional.of(openRequest));

    ServiceRequestResponse response = serviceRequestService.getRequestById(10L);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(10L);
  }

  @Test
  @DisplayName("Should throw RuntimeException when request is not found by ID")
  void shouldThrowRuntimeExceptionWhenRequestNotFoundById() {
    when(requestRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> serviceRequestService.getRequestById(99L));
  }

  // ── getRequestEntityById ──────────────────────────────────────────────────

  @Test
  @DisplayName("Should return service request entity when request exists")
  void shouldReturnServiceRequestEntityWhenFound() {
    when(requestRepository.findById(10L)).thenReturn(Optional.of(openRequest));

    ServiceRequest entity = serviceRequestService.getRequestEntityById(10L);

    assertThat(entity).isEqualTo(openRequest);
  }

  @Test
  @DisplayName("Should throw NotFoundException when request entity is not found by ID")
  void shouldThrowNotFoundExceptionWhenRequestEntityNotFound() {
    when(requestRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> serviceRequestService.getRequestEntityById(99L));
  }

  // ── createRequest ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("Should create a new service request and link department when departmentId is provided and found")
  void shouldCreateServiceRequestWithDepartment() {
    ServiceRequestDto dto = new ServiceRequestDto();
    dto.setTitle("Request With Dept");
    dto.setDescription("Description");
    dto.setCategory("IT_SUPPORT");
    dto.setPriority("HIGH");
    dto.setDepartmentId(5L);

    Department dept = Department.builder().id(5L).name("IT").build();

    when(departmentRepository.findById(5L)).thenReturn(Optional.of(dept));
    when(slaPolicyService.getResponseSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(2));
    when(slaPolicyService.getResolutionSlaDeadline(any(), any())).thenReturn(LocalDateTime.now().plusHours(8));
    when(requestRepository.save(any(ServiceRequest.class))).thenAnswer(inv -> {
      ServiceRequest req = inv.getArgument(0);
      req.setId(101L);
      return req;
    });

    ServiceRequestResponse response = serviceRequestService.createRequest(dto, regularUser);

    assertThat(response).isNotNull();
    verify(departmentRepository).findById(5L);
  }

  @Test
  @DisplayName("Should throw NotFoundException when creating a service request with a departmentId that does not exist")
  void shouldThrowNotFoundWhenDepartmentNotFound() {
    ServiceRequestDto dto = new ServiceRequestDto();
    dto.setTitle("Request With Unknown Dept");
    dto.setDescription("Description");
    dto.setCategory("IT_SUPPORT");
    dto.setPriority("LOW");
    dto.setDepartmentId(999L);

    when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class,
            () -> serviceRequestService.createRequest(dto, regularUser));

    verify(departmentRepository).findById(999L);
    verify(requestRepository, never()).save(any());
  }

  // ── updateStatus (with agent) ─────────────────────────────────────────────

  @Test
  @DisplayName("Should update status to ASSIGNED and set the assigned agent when a valid agent is provided")
  void shouldUpdateStatusToAssignedWithAgent() {
    when(requestRepository.findById(10L)).thenReturn(Optional.of(openRequest));
    when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StatusUpdateRequest update = new StatusUpdateRequest();
    update.setNewStatus(RequestStatus.ASSIGNED);

    ServiceRequestResponse response = serviceRequestService.updateStatus(10L, update, agentUser);

    assertThat(response.getStatus()).isEqualTo(RequestStatus.ASSIGNED.name());
    assertThat(openRequest.getAssignedTo()).isEqualTo(agentUser);
    assertThat(openRequest.getAssignedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw BadRequestException when updating to ASSIGNED status without providing an agent")
  void shouldThrowBadRequestWhenAssigningWithoutAgent() {
    when(requestRepository.findById(10L)).thenReturn(Optional.of(openRequest));

    StatusUpdateRequest update = new StatusUpdateRequest();
    update.setNewStatus(RequestStatus.ASSIGNED);

    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceRequestService.updateStatus(10L, update, null));

    assertThat(exception.getMessage()).isEqualTo("Agent must be provided when assigning a request");
  }

  @Test
  @DisplayName("Should update status to RESOLVED and record the resolved timestamp")
  void shouldUpdateStatusToResolvedAndRecordResolvedAt() {
    when(requestRepository.findById(12L)).thenReturn(Optional.of(inProgressRequest));
    when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StatusUpdateRequest update = new StatusUpdateRequest();
    update.setNewStatus(RequestStatus.RESOLVED);

    ServiceRequestResponse response = serviceRequestService.updateStatus(12L, update, null);

    assertThat(response.getStatus()).isEqualTo(RequestStatus.RESOLVED.name());
    assertThat(inProgressRequest.getResolvedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw NotFoundException when updating status of a non-existent request")
  void shouldThrowNotFoundWhenUpdatingStatusOfNonExistentRequest() {
    when(requestRepository.findById(99L)).thenReturn(Optional.empty());

    StatusUpdateRequest update = new StatusUpdateRequest();
    update.setNewStatus(RequestStatus.ASSIGNED);

    assertThrows(NotFoundException.class,
            () -> serviceRequestService.updateStatus(99L, update, agentUser));
  }

  // ── updateStatus (by agentId) ─────────────────────────────────────────────

  @Test
  @DisplayName("Should assign request to agent when calling updateStatus with agentId")
  void shouldAssignRequestToAgentWhenCalledWithAgentId() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(agentUser));
    when(requestRepository.findById(10L)).thenReturn(Optional.of(openRequest));
    when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ServiceRequestResponse response = serviceRequestService.updateStatus(10L, 2L);

    assertThat(response.getStatus()).isEqualTo(RequestStatus.ASSIGNED.name());
    verify(userRepository).findById(2L);
  }

  @Test
  @DisplayName("Should throw NotFoundException when agent is not found by agentId during assignment")
  void shouldThrowNotFoundWhenAgentNotFoundByAgentId() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
            () -> serviceRequestService.updateStatus(10L, 99L));
  }

  // ── updateStatus (status only, no agent) ─────────────────────────────────

  @Test
  @DisplayName("Should update status to CLOSED for an OPEN request when no agent context is needed")
  void shouldUpdateStatusToClosedWithNoAgentRequired() {
    when(requestRepository.findById(10L)).thenReturn(Optional.of(openRequest));
    when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    StatusUpdateRequest update = new StatusUpdateRequest();
    update.setNewStatus(RequestStatus.CLOSED);

    ServiceRequestResponse response = serviceRequestService.updateStatus(10L, update);

    assertThat(response.getStatus()).isEqualTo(RequestStatus.CLOSED.name());
  }

  // ── getRequestsForUser (role-based) ───────────────────────────────────────

  @Test
  @DisplayName("Should return all service requests when called for an ADMIN user")
  void shouldReturnAllRequestsForAdminUser() {
    when(requestRepository.findAll()).thenReturn(List.of(openRequest, assignedRequest, inProgressRequest));

    List<ServiceRequestResponse> result = serviceRequestService.getRequestsForUser(adminUser);

    assertThat(result).hasSize(3);
    verify(requestRepository).findAll();
  }

  @Test
  @DisplayName("Should return requests filtered by requester ID when called for a USER role")
  void shouldReturnRequestsByRequesterForUserRole() {
    when(requestRepository.findByRequesterId(3L)).thenReturn(List.of(openRequest));

    List<ServiceRequestResponse> result = serviceRequestService.getRequestsForUser(regularUser);

    assertThat(result).hasSize(1);
    verify(requestRepository).findByRequesterId(3L);
  }

  @Test
  @DisplayName("Should return requests filtered by department when called for an AGENT user")
  void shouldReturnRequestsByDepartmentForAgentUser() {
    agentUser.setDepartment(Department.builder().name("IT_SUPPORT").build());
    when(requestRepository.findByAssignedTo(agentUser)).thenReturn(List.of(assignedRequest));
    when(requestRepository.findByDepartmentAndAssignedToIsNullAndStatus(any(), eq(RequestStatus.OPEN))).thenReturn(List.of(openRequest));

    List<ServiceRequestResponse> result = serviceRequestService.getRequestsForUser(agentUser);

    assertThat(result).hasSize(2);
    verify(requestRepository).findByAssignedTo(agentUser);
    verify(requestRepository).findByDepartmentAndAssignedToIsNullAndStatus(any(), eq(RequestStatus.OPEN));
  }

  // ── getOpenRequestsForUser ────────────────────────────────────────────────

  @Test
  @DisplayName("Should return OPEN, IN_PROGRESS, and ASSIGNED requests for the given user")
  void shouldReturnOpenInProgressAndAssignedRequestsForUser() {
    when(requestRepository.findByRequesterAndStatusIn(regularUser,
            List.of(RequestStatus.OPEN, RequestStatus.IN_PROGRESS, RequestStatus.ASSIGNED)))
            .thenReturn(List.of(openRequest, inProgressRequest, assignedRequest));

    List<ServiceRequest> result = serviceRequestService.getOpenRequestsForUser(regularUser);

    assertThat(result).hasSize(3);
  }

  // ── getResolvedRequestsForUser ────────────────────────────────────────────

  @Test
  @DisplayName("Should return RESOLVED and CLOSED requests for the given user")
  void shouldReturnResolvedAndClosedRequestsForUser() {
    when(requestRepository.findByRequesterAndStatusIn(regularUser,
            List.of(RequestStatus.RESOLVED, RequestStatus.CLOSED)))
            .thenReturn(List.of(resolvedRequest, closedRequest));

    List<ServiceRequest> result = serviceRequestService.getResolvedRequestsForUser(regularUser);

    assertThat(result).hasSize(2);
  }

  // ── getAllRequests (list) ─────────────────────────────────────────────────

  @Test
  @DisplayName("Should return all service requests as a flat list when called without pagination")
  void shouldReturnAllServiceRequestsAsList() {
    when(requestRepository.findAll()).thenReturn(List.of(openRequest, assignedRequest));

    List<ServiceRequest> result = serviceRequestService.getAllRequests();

    assertThat(result).hasSize(2);
  }

  // ── getAssignedRequests ───────────────────────────────────────────────────

  @Test
  @DisplayName("Should return requests currently assigned to the given agent")
  void shouldReturnRequestsAssignedToAgent() {
    when(requestRepository.findByAssignedTo(agentUser)).thenReturn(List.of(assignedRequest));

    List<ServiceRequest> result = serviceRequestService.getAssignedRequests(agentUser);

    assertThat(result).hasSize(1);
    verify(requestRepository).findByAssignedTo(agentUser);
  }

  // ── getUnassignedRequests ─────────────────────────────────────────────────

  @Test
  @DisplayName("Should return all requests that have no assigned agent")
  void shouldReturnUnassignedRequests() {
    when(requestRepository.findByAssignedToIsNull()).thenReturn(List.of(openRequest));

    List<ServiceRequest> result = serviceRequestService.getUnassignedRequests();

    assertThat(result).hasSize(1);
    verify(requestRepository).findByAssignedToIsNull();
  }

  @Test
  @DisplayName("Should return all unassigned requests in department for ADMIN user")
  void shouldReturnAllUnassignedInDepartmentForAdminUser() {
    Department dept = Department.builder().id(5L).name("IT").build();
    when(requestRepository.findByDepartmentAndAssignedToIsNull(dept)).thenReturn(List.of(openRequest, assignedRequest));

    List<ServiceRequest> result = serviceRequestService.getUnassignedRequests(dept, Role.ADMIN);

    assertThat(result).hasSize(2);
    verify(requestRepository).findByDepartmentAndAssignedToIsNull(dept);
  }

  // ── getSlaBreachedRequests ────────────────────────────────────────────────

  @Test
  @DisplayName("Should return all requests that have breached their SLA")
  void shouldReturnSlaBreachedRequests() {
    when(requestRepository.findBySlaBreachedTrue()).thenReturn(List.of(openRequest));

    List<ServiceRequest> result = serviceRequestService.getSlaBreachedRequests();

    assertThat(result).hasSize(1);
    verify(requestRepository).findBySlaBreachedTrue();
  }

  // ── getSlaWarningRequests ─────────────────────────────────────────────────

  @Test
  @DisplayName("Should return requests approaching SLA deadline within the next 2 hours")
  void shouldReturnSlaWarningRequestsWithinTwoHours() {
    when(requestRepository.findSlaWarnings(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(openRequest));

    List<ServiceRequest> result = serviceRequestService.getSlaWarningRequests();

    assertThat(result).hasSize(1);
    verify(requestRepository).findSlaWarnings(any(LocalDateTime.class), any(LocalDateTime.class));
  }

  // ── validateStatusTransition (via reflection) ─────────────────────────────

  private void invokeValidate(RequestStatus current, RequestStatus next) throws Throwable {
    var method = ServiceRequestService.class
            .getDeclaredMethod("validateStatusTransition", RequestStatus.class, RequestStatus.class);
    method.setAccessible(true);
    try {
      method.invoke(serviceRequestService, current, next);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException when transitioning to the same request status")
  void shouldThrowBadRequestWhenTransitioningToSameStatus() {
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> invokeValidate(RequestStatus.OPEN, RequestStatus.OPEN));
    assertThat(exception.getMessage()).isEqualTo("Request is already in the desired status");
  }

  @Test
  @DisplayName("Should allow transition from OPEN to ASSIGNED")
  void shouldAllowSubmittedToAssignedTransition() {
    assertDoesNotThrow(() -> invokeValidate(RequestStatus.OPEN, RequestStatus.ASSIGNED));
  }

  @Test
  @DisplayName("Should allow transition from OPEN to CLOSED")
  void shouldAllowSubmittedToClosedTransition() {
    assertDoesNotThrow(() -> invokeValidate(RequestStatus.OPEN, RequestStatus.CLOSED));
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from OPEN to IN_PROGRESS")
  void shouldRejectSubmittedToInProgressTransition() {
    assertThrows(com.servicehub.exception.InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.OPEN, RequestStatus.IN_PROGRESS));
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from OPEN to RESOLVED")
  void shouldRejectOpenToResolvedTransition() {
    assertThrows(com.servicehub.exception.InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.OPEN, RequestStatus.RESOLVED));
  }

  @Test
  @DisplayName("Should allow transition from ASSIGNED to IN_PROGRESS")
  void shouldAllowAssignedToInProgressTransition() {
    assertDoesNotThrow(() -> invokeValidate(RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS));
  }

  @Test
  @DisplayName("Should allow transition from ASSIGNED to CLOSED")
  void shouldAllowAssignedToClosedTransition() {
    assertDoesNotThrow(() -> invokeValidate(RequestStatus.ASSIGNED, RequestStatus.CLOSED));
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from ASSIGNED to RESOLVED")
  void shouldRejectAssignedToResolvedTransition() {
    assertThrows(com.servicehub.exception.InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.ASSIGNED, RequestStatus.RESOLVED));
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from ASSIGNED to OPEN")
  void shouldRejectAssignedToOpenTransition() {
    assertThrows(com.servicehub.exception.InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.ASSIGNED, RequestStatus.OPEN));
  }

  @Test
  @DisplayName("Should allow transition from IN_PROGRESS to RESOLVED")
  void shouldAllowInProgressToResolvedTransition() {
    assertDoesNotThrow(() -> invokeValidate(RequestStatus.IN_PROGRESS, RequestStatus.RESOLVED));
  }

  @Test
  @DisplayName("Should allow transition from IN_PROGRESS to CLOSED")
  void shouldAllowInProgressToClosedTransition() {
    assertDoesNotThrow(() -> invokeValidate(RequestStatus.IN_PROGRESS, RequestStatus.CLOSED));
  }

  @Test
  @DisplayName("Should throw BadRequestException when transitioning from IN_PROGRESS to ASSIGNED")
  void shouldRejectInProgressToAssignedTransition() {
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> invokeValidate(RequestStatus.IN_PROGRESS, RequestStatus.ASSIGNED));
    assertThat(exception.getMessage()).isEqualTo("Invalid status transition from IN_PROGRESS to ASSIGNED");
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when attempting any transition from RESOLVED")
  void shouldRejectAnyTransitionFromResolvedStatus() {
    assertThrows(com.servicehub.exception.InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.RESOLVED, RequestStatus.CLOSED));
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when attempting any transition from CLOSED")
  void shouldRejectAnyTransitionFromClosedStatus() {
    assertThrows(com.servicehub.exception.InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.CLOSED, RequestStatus.OPEN));
  }
}
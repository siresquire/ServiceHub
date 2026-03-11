package com.servicehub.service;

import com.servicehub.exception.BadRequestException;
import com.servicehub.exception.InvalidServiceRequestTransition;
import com.servicehub.model.enums.RequestStatus;
import com.servicehub.repository.DepartmentRepository;
import com.servicehub.repository.ServiceRequestRepository;
import com.servicehub.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTests {

  @Mock
  private ServiceRequestRepository requestRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private DepartmentRepository departmentRepository;

  @InjectMocks
  private ServiceRequestService serviceRequestService;

  private void invokeValidate(RequestStatus current, RequestStatus next) throws Throwable {
    Method method = ServiceRequestService.class
            .getDeclaredMethod("validateStatusTransition", RequestStatus.class, RequestStatus.class);

    method.setAccessible(true);

    try {
      method.invoke(serviceRequestService, current, next);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException when transitioning to the same request status")
  void shouldThrowBadRequestWhenTransitioningToSameStatus() {

    BadRequestException exception = Assertions.assertThrows(
            BadRequestException.class,
            () -> invokeValidate(RequestStatus.OPEN, RequestStatus.OPEN)
    );

    Assertions.assertEquals(
            "Request is already in the desired status",
            exception.getMessage()
    );
  }

  @Test
  @DisplayName("Should allow transition from OPEN to ASSIGNED")
  void shouldAllowSubmittedToAssignedTransition() {

    Assertions.assertDoesNotThrow(
            () -> invokeValidate(RequestStatus.OPEN, RequestStatus.ASSIGNED)
    );
  }

  @Test
  @DisplayName("Should allow transition from OPEN to CLOSED")
  void shouldAllowSubmittedToClosedTransition() {

    Assertions.assertDoesNotThrow(
            () -> invokeValidate(RequestStatus.OPEN, RequestStatus.CLOSED)
    );
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from OPEN to IN_PROGRESS")
  void shouldRejectSubmittedToInProgressTransition() {

    InvalidServiceRequestTransition exception = Assertions.assertThrows(
            InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.OPEN, RequestStatus.IN_PROGRESS)
    );

    Assertions.assertEquals(
            "Invalid status transition from OPEN to IN_PROGRESS",
            exception.getMessage()
    );
  }

  @Test
  @DisplayName("Should allow transition from ASSIGNED to IN_PROGRESS")
  void shouldAllowAssignedToInProgressTransition() {

    Assertions.assertDoesNotThrow(
            () -> invokeValidate(RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS)
    );
  }

  @Test
  @DisplayName("Should allow transition from ASSIGNED to CLOSED")
  void shouldAllowAssignedToClosedTransition() {

    Assertions.assertDoesNotThrow(
            () -> invokeValidate(RequestStatus.ASSIGNED, RequestStatus.CLOSED)
    );
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from ASSIGNED to RESOLVED")
  void shouldRejectAssignedToResolvedTransition() {

    InvalidServiceRequestTransition exception = Assertions.assertThrows(
            InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.ASSIGNED, RequestStatus.RESOLVED)
    );

    Assertions.assertEquals(
            "Invalid status transition from ASSIGNED to RESOLVED",
            exception.getMessage()
    );
  }

  @Test
  @DisplayName("Should allow transition from IN_PROGRESS to RESOLVED")
  void shouldAllowInProgressToResolvedTransition() {

    Assertions.assertDoesNotThrow(
            () -> invokeValidate(RequestStatus.IN_PROGRESS, RequestStatus.RESOLVED)
    );
  }

  @Test
  @DisplayName("Should allow transition from IN_PROGRESS to CLOSED")
  void shouldAllowInProgressToClosedTransition() {

    Assertions.assertDoesNotThrow(
            () -> invokeValidate(RequestStatus.IN_PROGRESS, RequestStatus.CLOSED)
    );
  }

  @Test
  @DisplayName("Should throw BadRequestException when transitioning from IN_PROGRESS to ASSIGNED")
  void shouldRejectInProgressToAssignedTransition() {

    BadRequestException exception = Assertions.assertThrows(
            BadRequestException.class,
            () -> invokeValidate(RequestStatus.IN_PROGRESS, RequestStatus.ASSIGNED)
    );

    Assertions.assertEquals(
            "Invalid status transition from IN_PROGRESS to ASSIGNED",
            exception.getMessage()
    );
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from RESOLVED to CLOSED")
  void shouldRejectTransitionsFromResolvedStatus() {

    InvalidServiceRequestTransition exception = Assertions.assertThrows(
            InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.RESOLVED, RequestStatus.CLOSED)
    );

    Assertions.assertEquals(
            "Invalid status transition from RESOLVED to CLOSED",
            exception.getMessage()
    );
  }

  @Test
  @DisplayName("Should throw InvalidServiceRequestTransition when transitioning from CLOSED to OPEN")
  void shouldRejectTransitionsFromClosedStatus() {

    InvalidServiceRequestTransition exception = Assertions.assertThrows(
            InvalidServiceRequestTransition.class,
            () -> invokeValidate(RequestStatus.CLOSED, RequestStatus.OPEN)
    );

    Assertions.assertEquals(
            "Invalid status transition from CLOSED to OPEN",
            exception.getMessage()
    );
  }

}
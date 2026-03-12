package com.servicehub.repository;

import com.servicehub.model.ServiceRequest;
import com.servicehub.model.User;
import com.servicehub.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    Page<ServiceRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<ServiceRequest> findByStatus(RequestStatus status);
    List<ServiceRequest> findByCategory(RequestCategory category);
    List<ServiceRequest> findByAssignedToId(Long agentId);
    List<ServiceRequest> findByRequesterId(Long requesterId);

    //  this is for getting department object for AGENT
    List<ServiceRequest> findByDepartment_Name(String department);
    Long countByStatus(RequestStatus status);

    Page<ServiceRequest> findAllByStatus(RequestStatus status, Pageable pageable);

    Page<ServiceRequest> findByStatusOrStatus(RequestStatus requestStatus, RequestStatus requestStatus1, Pageable page);

    // Open requests for a user
    List<ServiceRequest> findByRequesterAndStatusIn(User requester, List<RequestStatus> statuses);

    // All requests for a user (all statuses)
    List<ServiceRequest> findByRequester(User requester);

    // Assigned to a specific agent
    List<ServiceRequest> findByAssignedTo(User agent);

    // Unassigned — no agent yet
    List<ServiceRequest> findByAssignedToIsNull();
    List<ServiceRequest> findByDepartmentAndAssignedToIsNull(com.servicehub.model.Department department);
    List<ServiceRequest> findByDepartmentAndAssignedToIsNullAndStatus(com.servicehub.model.Department department, RequestStatus status);

    // SLA breached
    List<ServiceRequest> findBySlaBreachedTrue();
    List<ServiceRequest> findByAssignedToAndSlaBreachedTrue(com.servicehub.model.User agent);
    List<ServiceRequest> findByDepartmentAndAssignedToIsNullAndStatusAndSlaBreachedTrue(com.servicehub.model.Department department, RequestStatus status);

    // SLA warning — deadline within next 2 hours
    @Query("SELECT s FROM ServiceRequest s WHERE s.slaBreached = false AND s.resolutionSlaDeadline IS NOT NULL AND s.resolutionSlaDeadline BETWEEN :now AND :cutoff")
    List<ServiceRequest> findSlaWarnings(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT s FROM ServiceRequest s WHERE s.assignedTo = :agent AND s.slaBreached = false AND s.resolutionSlaDeadline IS NOT NULL AND s.resolutionSlaDeadline BETWEEN :now AND :cutoff")
    List<ServiceRequest> findSlaWarningsForAgent(@Param("agent") com.servicehub.model.User agent, @Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT s FROM ServiceRequest s WHERE s.department = :department AND s.assignedTo IS NULL AND s.status = :status AND s.slaBreached = false AND s.resolutionSlaDeadline IS NOT NULL AND s.resolutionSlaDeadline BETWEEN :now AND :cutoff")
    List<ServiceRequest> findSlaWarningsForDepartmentUnassigned(@Param("department") com.servicehub.model.Department department, @Param("status") RequestStatus status, @Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);
}

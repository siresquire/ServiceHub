package com.servicehub.repository;

import com.servicehub.model.ServiceRequest;
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

    // ── AGENT DASHBOARD STATISTICS ──

    // Count tickets assigned to a specific agent
    Long countByAssignedToId(Long agentId);

    // Count unassigned tickets in a department (status is OPEN or ASSIGNED but no assignedTo)
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.department.name = :deptName AND sr.assignedTo IS NULL AND sr.status IN (:statuses)")
    Long countUnassignedByDepartment(@Param("deptName") String deptName, @Param("statuses") List<RequestStatus> statuses);

    // Count SLA breached tickets for an agent
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.assignedTo.id = :agentId AND sr.slaBreached = true AND sr.status NOT IN (com.servicehub.model.enums.RequestStatus.RESOLVED, com.servicehub.model.enums.RequestStatus.CLOSED)")
    Long countSlaBreachesByAgent(@Param("agentId") Long agentId);

    // Count SLA warning tickets (approaching deadline within 24 hours, not yet breached)
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.assignedTo.id = :agentId AND sr.slaBreached = false AND sr.resolutionSlaDeadline BETWEEN :now AND :warningTime AND sr.status NOT IN (com.servicehub.model.enums.RequestStatus.RESOLVED, com.servicehub.model.enums.RequestStatus.CLOSED)")
    Long countSlaWarningsByAgent(@Param("agentId") Long agentId, @Param("now") LocalDateTime now, @Param("warningTime") LocalDateTime warningTime);

    // Count SLA breaches in department (unassigned or assigned)
    @Query("SELECT COUNT(sr) FROM ServiceRequest sr WHERE sr.department.name = :deptName AND sr.slaBreached = true AND sr.status NOT IN (com.servicehub.model.enums.RequestStatus.RESOLVED, com.servicehub.model.enums.RequestStatus.CLOSED)")
    Long countSlaBreachesByDepartment(@Param("deptName") String deptName);
}

package com.servicehub.repository;

import com.servicehub.model.ServiceRequest;
import com.servicehub.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<ServiceRequest> findBySlaDeadlineBeforeAndStatusNotIn(LocalDateTime deadline, List<RequestStatus> statuses);

    Page<ServiceRequest> findAllByStatus(RequestStatus status, Pageable pageable);

    Page<ServiceRequest> findByStatusOrStatus(RequestStatus requestStatus, RequestStatus requestStatus1, Pageable page);
}

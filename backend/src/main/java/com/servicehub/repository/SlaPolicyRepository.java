package com.servicehub.repository;

import com.servicehub.model.SlaPolicy;
import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import org.apache.commons.lang3.concurrent.UncheckedFuture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    Optional<SlaPolicy> findByPriority(Priority priority);

    Optional<SlaPolicy> findByPriorityAndCategory(Priority priority, RequestCategory category);

    List<SlaPolicy> findAllByCategory(RequestCategory category);
}

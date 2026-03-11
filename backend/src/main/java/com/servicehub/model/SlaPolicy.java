package com.servicehub.model;

import com.servicehub.model.enums.Priority;
import com.servicehub.model.enums.RequestCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "sla_policies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SlaPolicy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RequestCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Priority priority;

    @Column(nullable = false, name = "response_hours")
    private Double responseTimeHours;

    @Column(nullable = false, name = "resolution_hours")
    private Double resolutionTimeHours;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}

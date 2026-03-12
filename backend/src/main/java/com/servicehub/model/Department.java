package com.servicehub.model;

import com.servicehub.model.enums.RequestCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "departments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Department {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

}

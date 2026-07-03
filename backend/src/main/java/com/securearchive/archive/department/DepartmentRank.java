package com.securearchive.archive.department;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(
    name = "department_ranks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_department_rank_code",
            columnNames = {"department_id", "code"}
        ),
        @UniqueConstraint(
            name = "uq_department_rank_name",
            columnNames = {"department_id", "name"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class DepartmentRank {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        // department_ranks.department_id → departments.id
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "department_id", nullable = false)
        private Department department;

        @Column(nullable = false, length = 50)
        private String code;

        @Column(nullable = false, length = 100)
        private String name;

        @Column(name = "level_order", nullable = false)
        private Integer levelOrder;

        @Column(columnDefinition = "TEXT")
        private String description;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;
}

package com.appointments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_breaks", indexes = {
    @Index(name = "idx_staff_time", columnList = "staff_id, start_time, end_time"),
    @Index(name = "idx_type", columnList = "breakType")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffProfile staff;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "break_type", nullable = false)
    private BreakType breakType = BreakType.RECURRING;

    @Column(name = "start_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime endTime;

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // For recurring breaks: 0=Sunday, 6=Saturday

    @Column(length = 255)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum BreakType {
        RECURRING, ONE_TIME
    }
}

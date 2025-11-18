package com.appointments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_user_time", columnList = "user_id, start_time"),
    @Index(name = "idx_staff_time", columnList = "staff_id, start_time"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "start_time"),
    @Index(name = "idx_staff_status_time", columnList = "staff_id, status, start_time"),
    @Index(name = "idx_user_status_time", columnList = "user_id, status, start_time")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffProfile staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "start_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "external_event_id", length = 500)
    private String externalEventId;

    @Builder.Default
    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    }
}

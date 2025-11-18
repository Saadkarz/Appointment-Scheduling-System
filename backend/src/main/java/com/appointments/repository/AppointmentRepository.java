package com.appointments.repository;

import com.appointments.entity.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId " +
           "AND a.status NOT IN ('CANCELLED') " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime) " +
           "OR (a.startTime < :endTime AND a.endTime > :endTime) " +
           "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
    List<Appointment> findConflictingAppointmentsForUpdate(
        @Param("staffId") Long staffId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    List<Appointment> findByUserId(Long userId);
    
    List<Appointment> findByStaffId(Long staffId);
    
    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId " +
           "AND a.startTime >= :from AND a.startTime < :to " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId " +
           "AND a.startTime >= :from AND a.startTime < :to " +
           "AND a.status NOT IN ('CANCELLED') " +
           "ORDER BY a.startTime ASC")
    List<Appointment> findByStaffIdAndDateRange(
        @Param("staffId") Long staffId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
    
    Optional<Appointment> findByExternalEventId(String externalEventId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.staff.id = :staffId " +
           "AND a.startTime >= :from AND a.startTime < :to " +
           "AND a.status = 'COMPLETED'")
    Long countCompletedAppointmentsByStaffAndDateRange(
        @Param("staffId") Long staffId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}

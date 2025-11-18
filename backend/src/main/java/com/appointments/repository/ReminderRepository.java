package com.appointments.repository;

import com.appointments.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    
    @Query("SELECT r FROM Reminder r WHERE r.scheduledTime <= :now " +
           "AND r.status = 'PENDING' " +
           "ORDER BY r.scheduledTime ASC")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);
    
    List<Reminder> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT r FROM Reminder r WHERE r.status = 'FAILED' " +
           "AND r.retryCount < :maxRetries")
    List<Reminder> findFailedRemindersForRetry(@Param("maxRetries") Integer maxRetries);
}

package com.appointments.repository;

import com.appointments.entity.StaffBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StaffBreakRepository extends JpaRepository<StaffBreak, Long> {
    
    @Query("SELECT sb FROM StaffBreak sb WHERE sb.staff.id = :staffId " +
           "AND ((sb.breakType = 'ONE_TIME' AND sb.startTime >= :from AND sb.startTime < :to) " +
           "OR (sb.breakType = 'RECURRING' AND sb.dayOfWeek = :dayOfWeek))")
    List<StaffBreak> findByStaffIdAndDateRange(
        @Param("staffId") Long staffId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("dayOfWeek") Integer dayOfWeek
    );
}

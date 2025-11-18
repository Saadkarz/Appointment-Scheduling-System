package com.appointments.repository;

import com.appointments.entity.StaffWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffWorkingHoursRepository extends JpaRepository<StaffWorkingHours, Long> {
    
    List<StaffWorkingHours> findByStaffIdAndIsActiveTrue(Long staffId);
    
    @Query("SELECT swh FROM StaffWorkingHours swh WHERE swh.staff.id = :staffId " +
           "AND swh.dayOfWeek = :dayOfWeek AND swh.isActive = true")
    List<StaffWorkingHours> findByStaffIdAndDayOfWeek(
        @Param("staffId") Long staffId,
        @Param("dayOfWeek") Integer dayOfWeek
    );
}

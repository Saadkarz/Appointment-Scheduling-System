package com.appointments.repository;

import com.appointments.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    
    Optional<StaffProfile> findByUserId(Long userId);
    
    List<StaffProfile> findByIsAvailableTrue();
    
    @Query("SELECT sp FROM StaffProfile sp WHERE sp.isAvailable = true AND sp.id = :staffId")
    Optional<StaffProfile> findAvailableStaffById(@Param("staffId") Long staffId);
}

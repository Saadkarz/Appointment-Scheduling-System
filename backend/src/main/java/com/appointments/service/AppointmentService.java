package com.appointments.service;

import com.appointments.dto.AppointmentRequest;
import com.appointments.dto.AppointmentResponse;
import com.appointments.entity.*;
import com.appointments.exception.ResourceNotFoundException;
import com.appointments.exception.AppointmentConflictException;
import com.appointments.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final ServiceRepository serviceRepository;
    private final ReminderService reminderService;
    private final CalendarSyncService calendarSyncService;

    /**
     * Create appointment with atomic conflict detection using SELECT FOR UPDATE
     */
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request, String userEmail) {
        log.info("Creating appointment for user: {}", userEmail);

        // Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate staff
        StaffProfile staff = staffProfileRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        // Validate service
        com.appointments.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();

        // Atomic conflict detection with pessimistic locking
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointmentsForUpdate(
                staff.getId(), startTime, endTime
        );

        if (!conflicts.isEmpty()) {
            log.warn("Appointment conflict detected for staff {} at time {}", staff.getId(), startTime);
            throw new AppointmentConflictException("This time slot is no longer available");
        }

        // Create appointment
        Appointment appointment = Appointment.builder()
                .user(user)
                .staff(staff)
                .service(service)
                .startTime(startTime)
                .endTime(endTime)
                .status(Appointment.Status.PENDING)
                .notes(request.getNotes())
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment created with ID: {}", appointment.getId());

        // Create reminders (24h, 1h, 15min before)
        reminderService.createRemindersForAppointment(appointment);

        // Sync with external calendar
        try {
            calendarSyncService.syncAppointmentToCalendar(appointment);
        } catch (Exception e) {
            log.error("Failed to sync appointment to calendar", e);
            // Continue even if calendar sync fails
        }

        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long appointmentId, AppointmentRequest request, String userEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Check authorization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!appointment.getUser().getId().equals(user.getId()) &&
            !user.getRole().equals(User.Role.ADMIN) &&
            !user.getRole().equals(User.Role.STAFF)) {
            throw new SecurityException("Not authorized to update this appointment");
        }

        // Update fields
        if (request.getStartTime() != null && request.getEndTime() != null) {
            // Check for conflicts if time is changed
            if (!request.getStartTime().equals(appointment.getStartTime())) {
                List<Appointment> conflicts = appointmentRepository.findConflictingAppointmentsForUpdate(
                        appointment.getStaff().getId(),
                        request.getStartTime(),
                        request.getEndTime()
                );

                // Exclude current appointment from conflicts
                conflicts = conflicts.stream()
                        .filter(a -> !a.getId().equals(appointmentId))
                        .collect(Collectors.toList());

                if (!conflicts.isEmpty()) {
                    throw new AppointmentConflictException("This time slot is not available");
                }

                appointment.setStartTime(request.getStartTime());
                appointment.setEndTime(request.getEndTime());

                // Update reminders
                reminderService.updateRemindersForAppointment(appointment);
            }
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        appointment = appointmentRepository.save(appointment);

        // Update calendar event
        try {
            calendarSyncService.updateCalendarEvent(appointment);
        } catch (Exception e) {
            log.error("Failed to update calendar event", e);
        }

        return mapToResponse(appointment);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String reason, String userEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Check authorization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!appointment.getUser().getId().equals(user.getId()) &&
            !user.getRole().equals(User.Role.ADMIN)) {
            throw new SecurityException("Not authorized to cancel this appointment");
        }

        appointment.setStatus(Appointment.Status.CANCELLED);
        appointment.setCancellationReason(reason);
        appointmentRepository.save(appointment);

        // Cancel reminders
        reminderService.cancelRemindersForAppointment(appointmentId);

        // Delete calendar event
        try {
            calendarSyncService.deleteCalendarEvent(appointment);
        } catch (Exception e) {
            log.error("Failed to delete calendar event", e);
        }

        log.info("Appointment {} cancelled", appointmentId);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getStaffAppointments(Long staffId, LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findByStaffIdAndDateRange(staffId, from, to).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        return mapToResponse(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .userId(appointment.getUser().getId())
                .staffId(appointment.getStaff().getId())
                .serviceId(appointment.getService().getId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .notes(appointment.getNotes())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}

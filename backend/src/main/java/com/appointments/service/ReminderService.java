package com.appointments.service;

import com.appointments.entity.Appointment;
import com.appointments.entity.Reminder;
import com.appointments.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;

    private static final int REMINDER_OFFSET_MINUTES = 15;

    @Transactional
    public void createRemindersForAppointment(Appointment appointment) {
        List<Reminder> reminders = new ArrayList<>();

        // 24 hours before
        reminders.add(createReminder(
                appointment,
                appointment.getStartTime().minusHours(24),
                Reminder.Type.EMAIL
        ));

        // 1 hour before
        reminders.add(createReminder(
                appointment,
                appointment.getStartTime().minusHours(1),
                Reminder.Type.EMAIL
        ));

        // 15 minutes before (SMS)
        reminders.add(createReminder(
                appointment,
                appointment.getStartTime().minusMinutes(REMINDER_OFFSET_MINUTES),
                Reminder.Type.SMS
        ));

        reminderRepository.saveAll(reminders);
        log.info("Created {} reminders for appointment {}", reminders.size(), appointment.getId());
    }

    @Transactional
    public void updateRemindersForAppointment(Appointment appointment) {
        // Cancel old reminders
        cancelRemindersForAppointment(appointment.getId());

        // Create new reminders
        createRemindersForAppointment(appointment);
    }

    @Transactional
    public void cancelRemindersForAppointment(Long appointmentId) {
        List<Reminder> reminders = reminderRepository.findByAppointmentId(appointmentId);
        reminders.forEach(reminder -> {
            if (reminder.getStatus() == Reminder.Status.PENDING) {
                reminder.setStatus(Reminder.Status.CANCELLED);
            }
        });
        reminderRepository.saveAll(reminders);
        log.info("Cancelled {} reminders for appointment {}", reminders.size(), appointmentId);
    }

    @Transactional
    public void processDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findDueReminders(now);

        log.info("Processing {} due reminders", dueReminders.size());

        for (Reminder reminder : dueReminders) {
            try {
                sendReminder(reminder);
            } catch (Exception e) {
                log.error("Failed to send reminder {}", reminder.getId(), e);
                handleReminderFailure(reminder, e.getMessage());
            }
        }
    }

    private void sendReminder(Reminder reminder) {
        Appointment appointment = reminder.getAppointment();

        String subject = "Appointment Reminder";
        String message = String.format(
                "Reminder: You have an appointment on %s with %s %s",
                appointment.getStartTime(),
                appointment.getStaff().getUser().getFirstName(),
                appointment.getStaff().getUser().getLastName()
        );

        boolean sent = false;

        switch (reminder.getType()) {
            case EMAIL:
                sent = notificationService.sendEmail(
                        reminder.getRecipient(),
                        subject,
                        message
                );
                break;
            case SMS:
                sent = notificationService.sendSMS(
                        reminder.getRecipient(),
                        message
                );
                break;
            case PUSH:
                // Push notifications not yet implemented
                log.warn("Push notifications not yet implemented for reminder {}", reminder.getId());
                sent = false;
                break;
        }

        if (sent) {
            reminder.setStatus(Reminder.Status.SENT);
            reminder.setSentAt(LocalDateTime.now());
            log.info("Reminder {} sent successfully", reminder.getId());
        } else {
            throw new RuntimeException("Failed to send reminder");
        }

        reminderRepository.save(reminder);
    }

    private void handleReminderFailure(Reminder reminder, String errorMessage) {
        reminder.setStatus(Reminder.Status.FAILED);
        reminder.setRetryCount(reminder.getRetryCount() + 1);
        reminder.setErrorMessage(errorMessage);

        // Reschedule with exponential backoff (max 3 retries)
        if (reminder.getRetryCount() < 3) {
            long backoffMinutes = (long) Math.pow(2, reminder.getRetryCount()) * 5;
            reminder.setScheduledTime(LocalDateTime.now().plusMinutes(backoffMinutes));
            reminder.setStatus(Reminder.Status.PENDING);
            log.info("Rescheduling reminder {} in {} minutes", reminder.getId(), backoffMinutes);
        }

        reminderRepository.save(reminder);
    }

    private Reminder createReminder(Appointment appointment, LocalDateTime scheduledTime, Reminder.Type type) {
        String recipient = type == Reminder.Type.EMAIL
                ? appointment.getUser().getEmail()
                : appointment.getUser().getPhone();

        return Reminder.builder()
                .appointment(appointment)
                .type(type)
                .recipient(recipient)
                .scheduledTime(scheduledTime)
                .status(Reminder.Status.PENDING)
                .retryCount(0)
                .build();
    }
}

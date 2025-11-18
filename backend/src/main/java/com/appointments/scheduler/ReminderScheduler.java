package com.appointments.scheduler;

import com.appointments.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;

    /**
     * Process due reminders every minute
     */
    @Scheduled(cron = "0 * * * * *")
    public void processReminders() {
        log.debug("Running reminder scheduler");
        try {
            reminderService.processDueReminders();
        } catch (Exception e) {
            log.error("Error processing reminders", e);
        }
    }
}

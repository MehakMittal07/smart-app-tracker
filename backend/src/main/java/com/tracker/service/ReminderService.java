package com.tracker.service;

import com.tracker.model.Opportunity;
import com.tracker.model.Reminder;
import com.tracker.model.enums.OpportunityStatus;
import com.tracker.repository.OpportunityRepository;
import com.tracker.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final OpportunityRepository opportunityRepository;
    private final ReminderRepository    reminderRepository;
    private final JavaMailSender        mailSender;

    // ── Every day at 9 AM — schedule upcoming reminders ──────────────────
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void scheduleReminders() {
        log.info("=== scheduleReminders triggered ===");

        LocalDate today  = LocalDate.now();
        LocalDate cutoff = today.plusDays(7);

        List<Opportunity> upcoming = opportunityRepository.findAll()
                .stream()
                .filter(o -> o.getStatus() == OpportunityStatus.PENDING)
                .filter(o -> o.getDeadline() != null)
                .filter(o -> !o.getDeadline().isBefore(today))
                .filter(o -> !o.getDeadline().isAfter(cutoff))
                .toList();

        log.info("Found {} upcoming opportunities", upcoming.size());
        upcoming.forEach(this::scheduleReminderFor);
    }

    // ── Every hour — send reminders that are due ──────────────────────────
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendDueReminders() {
        log.info("=== sendDueReminders triggered ===");

        List<Reminder> due = reminderRepository
                .findDueReminders(LocalDateTime.now());

        log.info("Found {} due reminders", due.size());

        for (Reminder reminder : due) {
            try {
                sendReminderEmail(reminder);
                reminder.setSent(true);
                reminder.setSentAt(LocalDateTime.now());
                reminderRepository.save(reminder);
                log.info("Sent reminder for: {}", reminder.getOpportunity().getTitle());
            } catch (Exception e) {
                log.error("Failed reminder id={}: {}", reminder.getId(), e.getMessage());
            }
        }
    }

    // ── Every day at midnight — auto-expire passed deadlines ──────────────
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expirePassedDeadlines() {
        log.info("=== expirePassedDeadlines triggered ===");

        List<Opportunity> expired = opportunityRepository
                .findByStatusAndDeadlineBefore(OpportunityStatus.PENDING, LocalDate.now());

        expired.forEach(o -> {
            o.setStatus(OpportunityStatus.EXPIRED);
            opportunityRepository.save(o);
            log.info("Auto-expired: {}", o.getTitle());
        });

        log.info("Expired {} opportunities", expired.size());
    }

    // ── Create reminder entries for 7d / 3d / 1d before deadline ─────────
    public void scheduleReminderFor(Opportunity opp) {
        int[] daysBefore = {7, 3, 1};

        for (int days : daysBefore) {
            LocalDateTime reminderTime = opp.getDeadline()
                    .minusDays(days)
                    .atTime(9, 0);

            if (reminderTime.isBefore(LocalDateTime.now())) continue;

            if (reminderRepository.existsByOpportunityIdAndScheduledAt(
                    opp.getId(), reminderTime)) continue;

            Reminder reminder = Reminder.builder()
                    .opportunity(opp)
                    .scheduledAt(reminderTime)
                    .sent(false)
                    .build();

            reminderRepository.save(reminder);
            log.info("Scheduled {}d reminder for: {}", days, opp.getTitle());
        }
    }

    // ── Build and send the reminder email ─────────────────────────────────
    private void sendReminderEmail(Reminder reminder) {
        Opportunity opp   = reminder.getOpportunity();
        String      email = opp.getUser().getEmail();
        long        days  = ChronoUnit.DAYS.between(LocalDate.now(), opp.getDeadline());

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Deadline Reminder: " + opp.getTitle());
        msg.setText("""
            Hi there!
            
            Reminder about an upcoming deadline:
            
            Opportunity : %s
            Deadline    : %s
            Days Left   : %d day%s
            Apply Link  : %s
            
            Don't miss this opportunity!
            
            — Smart App Tracker
            """.formatted(
                opp.getTitle(),
                opp.getDeadline(),
                days,
                days == 1 ? "" : "s",
                opp.getApplicationLink() != null ? opp.getApplicationLink() : "N/A"
        ));

        mailSender.send(msg);
        log.info("Email sent to {} for: {}", email, opp.getTitle());
    }
}
//```
//
//        ---
//
//        Rebuild and restart — the app will start cleanly. Your complete project is now fully done:
//        ```
//        ✓ Phase 1 — Schema + structure
//        ✓ Phase 2 — Google OAuth2 + JWT
//        ✓ Phase 3 — Gmail fetch + parser
//        ✓ Phase 4 — REST APIs + React dashboard
//        ✓ Phase 5 — Scheduler + reminders + auto-expire
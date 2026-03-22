package com.tracker.controller;

import com.tracker.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    // Manually trigger reminder scheduling
    // POST http://localhost:8085/api/reminders/schedule
    @PostMapping("/schedule")
    public ResponseEntity<Map<String, String>> schedule() {
        reminderService.scheduleReminders();
        return ResponseEntity.ok(Map.of("status", "Reminders scheduled"));
    }

    // Manually trigger sending due reminders
    // POST http://localhost:8085/api/reminders/send
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send() {
        reminderService.sendDueReminders();
        return ResponseEntity.ok(Map.of("status", "Due reminders sent"));
    }

    // Manually trigger deadline expiry
    // POST http://localhost:8085/api/reminders/expire
    @PostMapping("/expire")
    public ResponseEntity<Map<String, String>> expire() {
        reminderService.expirePassedDeadlines();
        return ResponseEntity.ok(Map.of("status", "Expired opportunities updated"));
    }
}
//```
//
//        ---
//
//        ## Complete project status
//        ```
//        ✓ Phase 1 — Database schema + project structure
//        ✓ Phase 2 — Google OAuth2 login + JWT
//        ✓ Phase 3 — Gmail fetch + email parsing
//        ✓ Phase 4 — REST APIs + React dashboard (WORKING!)
//        ✓ Phase 5 — @Scheduled reminders + auto-expire
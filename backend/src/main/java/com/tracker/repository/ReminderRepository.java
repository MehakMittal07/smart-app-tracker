package com.tracker.repository;

import com.tracker.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // JOIN FETCH must be in FROM clause — fixed JPQL
    @Query("SELECT r FROM Reminder r " +
            "JOIN FETCH r.opportunity o " +
            "JOIN FETCH o.user " +
            "WHERE r.sent = false " +
            "AND r.scheduledAt <= :now")
    List<Reminder> findDueReminders(LocalDateTime now);

    // Derived query — no JPQL needed, Spring Data generates it
    boolean existsByOpportunityIdAndScheduledAt(
            Long opportunityId, LocalDateTime scheduledAt);
}
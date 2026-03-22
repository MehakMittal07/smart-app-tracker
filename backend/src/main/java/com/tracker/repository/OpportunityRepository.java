package com.tracker.repository;

import com.tracker.model.Opportunity;
import com.tracker.model.enums.OpportunityStatus;
import com.tracker.model.enums.OpportunityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    // Used by GmailService to skip already-processed emails
    boolean existsBySourceEmailId(String sourceEmailId);

    // All opportunities for a user, newest first
    List<Opportunity> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Filter by type and/or status
    List<Opportunity> findByUserIdAndTypeAndStatus(
            Long userId, OpportunityType type, OpportunityStatus status);

    // Deadlines approaching in next N days — used by ReminderService (Phase 5)
    @Query("SELECT o FROM Opportunity o WHERE o.user.id = :userId " +
            "AND o.deadline BETWEEN :today AND :cutoff " +
            "AND o.status = 'PENDING'")
    List<Opportunity> findUpcomingDeadlines(
            Long userId, LocalDate today, LocalDate cutoff);

    // Search by keyword in title
    @Query("SELECT o FROM Opportunity o WHERE o.user.id = :userId " +
            "AND LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Opportunity> searchByTitle(Long userId, String keyword);

    // Expired opportunities to auto-close
    List<Opportunity> findByStatusAndDeadlineBefore(
            OpportunityStatus status, LocalDate date);
}
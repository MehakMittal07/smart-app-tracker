// com/tracker/model/UserPreference.java
package com.tracker.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preferences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "reminder_days_before")
    private Integer reminderDaysBefore = 3;

    @Builder.Default
    @Column(name = "email_reminders_enabled")
    private Boolean emailRemindersEnabled = true;

    @Column(name = "keyword_filters", columnDefinition = "TEXT")
    private String keywordFilters;  // stored as JSON string, e.g. ["AWS","Google"]
}
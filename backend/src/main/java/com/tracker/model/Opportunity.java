package com.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tracker.model.enums.OpportunityStatus;
import com.tracker.model.enums.OpportunityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler",
            "opportunities", "googleRefreshToken", "providerId"})
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "application_link", columnDefinition = "TEXT")
    private String applicationLink;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityStatus status = OpportunityStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityType type = OpportunityType.OTHER;

    @Column(name = "source_email_id", unique = true, length = 64)
    private String sourceEmailId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
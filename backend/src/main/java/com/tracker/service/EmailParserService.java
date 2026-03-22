package com.tracker.service;

import com.tracker.model.Opportunity;
import com.tracker.model.User;
import com.tracker.model.enums.OpportunityStatus;
import com.tracker.model.enums.OpportunityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.*;

@Slf4j
@Service
public class EmailParserService {

    // ── Date formats we attempt to parse ──────────────────────────────────
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("MMMM d, yyyy",   Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM dd, yyyy",  Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy",    Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd, yyyy",   Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMMM yyyy",   Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yyyy",    Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    // ── Deadline trigger phrases ───────────────────────────────────────────
    private static final Pattern DEADLINE_PATTERN = Pattern.compile(
            "(?i)(?:deadline|last date|closing date|apply by|due date|" +
                    "applications? close[sd]?|submit(?:ted)? by|expires?|" +
                    "last day to apply)[:\\s\\-–]*" +
                    "([A-Za-z]+\\s+\\d{1,2}(?:st|nd|rd|th)?,?\\s+\\d{4}" + // Jan 15, 2025
                    "|\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}"               + // 15/01/2025
                    "|\\d{4}-\\d{2}-\\d{2})",                                // 2025-01-15
            Pattern.CASE_INSENSITIVE
    );

    // ── URL extractor ─────────────────────────────────────────────────────
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+"
    );

    // ── Application-specific link keywords ───────────────────────────────
    private static final Pattern APPLY_LINK_PATTERN = Pattern.compile(
            "(?i)(?:apply now|apply here|click here to apply|" +
                    "register now|submit application|application link)[^\\n]*" +
                    "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)"
    );

    // ── Title extraction — looks for internship/cert name near keywords ──
    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "(?i)(?:internship|fellowship|certification|program|course|" +
                    "hackathon|bootcamp|scholarship|opportunity)[:\\s]+([^\\n.!?]{5,80})"
    );

    // ── Keyword sets for classification ───────────────────────────────────
    private static final Set<String> INTERNSHIP_KEYWORDS = Set.of(
            "internship", "intern", "fellowship", "trainee", "apprenticeship",
            "summer program", "industrial training", "placement"
    );
    private static final Set<String> CERTIFICATION_KEYWORDS = Set.of(
            "certification", "certificate", "course", "workshop", "bootcamp",
            "udemy", "coursera", "edx", "nptel", "mooc", "training program"
    );
    private static final Set<String> HACKATHON_KEYWORDS = Set.of(
            "hackathon", "datathon", "coding challenge", "competition",
            "contest", "ideathon", "buildathon"
    );

    // Minimum relevance score to save (tune this as needed)
    private static final int MIN_SCORE = 2;

    // ── Entry point ────────────────────────────────────────────────────────
    public Optional<Opportunity> parse(String subject,
                                       String body,
                                       String emailId,
                                       User user) {
        String combined = (subject + " " + body).toLowerCase();

        int score = computeRelevanceScore(combined);
        if (score < MIN_SCORE) {
            log.debug("Email {} scored {} — below threshold, skipping", emailId, score);
            return Optional.empty();
        }

        String title           = extractTitle(subject, body);
        LocalDate deadline     = extractDeadline(body);
        String applicationLink = extractApplicationLink(body);
        OpportunityType type   = classifyType(combined);

        Opportunity opp = Opportunity.builder()
                .title(title)
                .deadline(deadline)
                .applicationLink(applicationLink)
                .type(type)
                .status(OpportunityStatus.PENDING)
                .sourceEmailId(emailId)
                .user(user)
                .build();

        return Optional.of(opp);
    }

    // ── Relevance scoring — how "opportunity-like" is this email? ─────────
    private int computeRelevanceScore(String text) {
        int score = 0;
        String[] highValue = {
                "internship", "fellowship", "apply now", "deadline",
                "certification", "hackathon", "last date", "closing date",
                "application open", "registration open"
        };
        String[] lowValue = {
                "opportunity", "program", "course", "workshop",
                "register", "submit", "scholarship"
        };
        for (String kw : highValue) if (text.contains(kw)) score += 2;
        for (String kw : lowValue)  if (text.contains(kw)) score += 1;
        return score;
    }

    // ── Title extraction ──────────────────────────────────────────────────
    private String extractTitle(String subject, String body) {
        // 1. Try subject line first — usually the most concise title
        if (subject != null && !subject.isBlank()) {
            String cleaned = subject
                    .replaceAll("(?i)^(re|fwd|fw):\\s*", "")
                    .replaceAll("\\[.*?\\]", "")
                    .trim();
            if (cleaned.length() >= 5) return truncate(cleaned, 200);
        }

        // 2. Fallback: look for title-like patterns in first 500 chars of body
        Matcher m = TITLE_PATTERN.matcher(body.substring(0, Math.min(500, body.length())));
        if (m.find()) return truncate(m.group(1).trim(), 200);

        return "Untitled Opportunity";
    }

    // ── Deadline extraction ───────────────────────────────────────────────
    private LocalDate extractDeadline(String body) {
        Matcher m = DEADLINE_PATTERN.matcher(body);
        while (m.find()) {
            String raw = m.group(1)
                    .replaceAll("(?i)(st|nd|rd|th),?", "") // strip ordinals
                    .trim();
            LocalDate date = tryParseDate(raw);
            if (date != null && date.isAfter(LocalDate.now())) {
                return date; // return first future deadline found
            }
        }
        return null; // deadline not found — user can fill it in via UI
    }

    private LocalDate tryParseDate(String raw) {
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(raw, fmt);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    // ── URL / apply link extraction ───────────────────────────────────────
    private String extractApplicationLink(String body) {
        // Priority 1: link right after "Apply Now", "Register Here" etc.
        Matcher applyMatcher = APPLY_LINK_PATTERN.matcher(body);
        if (applyMatcher.find()) return applyMatcher.group(1);

        // Priority 2: any URL in the body — pick the longest (most specific)
        Matcher urlMatcher = URL_PATTERN.matcher(body);
        String bestUrl = null;
        int    bestLen = 0;
        while (urlMatcher.find()) {
            String url = urlMatcher.group();
            // Skip tracking pixels, unsubscribe links, and tiny URLs
            if (url.contains("unsubscribe") || url.contains("pixel")
                    || url.contains("track")    || url.length() < 20) continue;
            if (url.length() > bestLen) {
                bestLen = url.length();
                bestUrl = url;
            }
        }
        return bestUrl;
    }

    // ── Type classification ───────────────────────────────────────────────
    private OpportunityType classifyType(String text) {
        if (HACKATHON_KEYWORDS.stream().anyMatch(text::contains))
            return OpportunityType.HACKATHON;
        if (INTERNSHIP_KEYWORDS.stream().anyMatch(text::contains))
            return OpportunityType.INTERNSHIP;
        if (CERTIFICATION_KEYWORDS.stream().anyMatch(text::contains))
            return OpportunityType.CERTIFICATION;
        return OpportunityType.OTHER;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
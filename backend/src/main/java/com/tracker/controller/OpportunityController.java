package com.tracker.controller;

import com.tracker.exception.ResourceNotFoundException;
import com.tracker.model.Opportunity;
import com.tracker.model.enums.OpportunityStatus;
import com.tracker.model.enums.OpportunityType;
import com.tracker.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityRepository opportunityRepository;

    // GET /api/opportunities  — all for current user
    @GetMapping
    public ResponseEntity<List<Opportunity>> getAll(Authentication auth) {
        Long userId = (Long) auth.getDetails();
        return ResponseEntity.ok(
                opportunityRepository.findByUserIdOrderByCreatedAtDesc(userId)
        );
    }

    // GET /api/opportunities/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Opportunity> getById(@PathVariable Long id,
                                               Authentication auth) {
        Long userId = (Long) auth.getDetails();
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", "id", id));
        return ResponseEntity.ok(opp);
    }

    // PATCH /api/opportunities/{id}/status  — update status (Applied, Saved…)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Opportunity> updateStatus(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body,
                                                    Authentication auth) {
        Long userId = (Long) auth.getDetails();
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", "id", id));

        OpportunityStatus newStatus = OpportunityStatus.valueOf(body.get("status").toUpperCase());
        opp.setStatus(newStatus);
        return ResponseEntity.ok(opportunityRepository.save(opp));
    }

    // GET /api/opportunities/filter?type=INTERNSHIP&status=PENDING
    @GetMapping("/filter")
    public ResponseEntity<List<Opportunity>> filter(
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(required = false) OpportunityStatus status,
            Authentication auth) {
        Long userId = (Long) auth.getDetails();

        List<Opportunity> all =
                opportunityRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return ResponseEntity.ok(all.stream()
                .filter(o -> type   == null || o.getType().equals(type))
                .filter(o -> status == null || o.getStatus().equals(status))
                .toList());
    }

    // GET /api/opportunities/search?keyword=google
    @GetMapping("/search")
    public ResponseEntity<List<Opportunity>> search(@RequestParam String keyword,
                                                    Authentication auth) {
        Long userId = (Long) auth.getDetails();
        return ResponseEntity.ok(
                opportunityRepository.searchByTitle(userId, keyword)
        );
    }

    // DELETE /api/opportunities/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       Authentication auth) {
        Long userId = (Long) auth.getDetails();
        Opportunity opp = opportunityRepository.findById(id)
                .filter(o -> o.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", "id", id));
        opportunityRepository.delete(opp);
        return ResponseEntity.noContent().build();
    }
}
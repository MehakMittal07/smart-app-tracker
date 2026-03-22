// src/main/java/com/tracker/model/enums/OpportunityStatus.java
package com.tracker.model.enums;

public enum OpportunityStatus {
    PENDING,    // not yet actioned
    APPLIED,    // user clicked Apply
    SAVED,      // bookmarked for later
    REJECTED,   // didn't get it
    EXPIRED     // deadline passed
}
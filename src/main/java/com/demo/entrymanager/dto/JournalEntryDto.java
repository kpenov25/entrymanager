package com.demo.entrymanager.dto;

import com.demo.entrymanager.model.Status;

import java.time.LocalDateTime;

public record JournalEntryDto(
        Long id,
        String scenario,
        Status status,
        LocalDateTime draftedDate,
        LocalDateTime reviewedDate,
        LocalDateTime approvedDate,
        String assignedAccountant,
        String reviewNotes,
        String approveNotes
) {
}

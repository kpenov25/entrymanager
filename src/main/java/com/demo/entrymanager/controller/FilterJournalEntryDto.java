package com.demo.entrymanager.controller;

import com.demo.entrymanager.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public record FilterJournalEntryDto(
        List<Status> status,
        LocalDateTime draftedDate,
        LocalDateTime reviewedDate,
        String assignedAccountant) {
}

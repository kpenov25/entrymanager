package com.demo.entrymanager.repository;

import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface JournalEntryFilterRepository {
    List<JournalEntry> findWithFilters(List<Status> statuses, LocalDateTime startDate,
                                       LocalDateTime endDate, String assignedAccountant);
}

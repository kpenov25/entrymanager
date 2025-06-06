package com.demo.entrymanager.repository;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findWithFilters(List<Status> status,
                                          LocalDateTime draftedDate,
                                          LocalDateTime reviewedDate,
                                          String assignedAccountant);
}

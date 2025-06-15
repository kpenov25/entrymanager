package com.demo.entrymanager.repository;

import com.demo.entrymanager.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JournalEntryFilterRepository {

}

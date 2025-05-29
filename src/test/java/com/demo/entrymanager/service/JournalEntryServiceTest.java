package com.demo.entrymanager.service;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.repository.JournalEntryRepository;
import com.demo.entrymanager.service.impl.JournalEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JournalEntryServiceTest {
    private JournalEntryService journalEntryService;
    @Mock
    private JournalEntryRepository journalEntryRepository;

    @BeforeEach
    void setup(){
        journalEntryService = new JournalEntryServiceImpl(journalEntryRepository);
    }

    @Test
    void givenJournalEntryDetails_whenJournalEntryIsCreated_thenCallsJournalEntryRepositorySave(){
        final JournalEntryDto journalEntryDto = new JournalEntryDto(
                null, "test scenario", null, null, null, null, null, null);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(new JournalEntry());

        journalEntryService.createJournalEntry(journalEntryDto);

        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
    }
}

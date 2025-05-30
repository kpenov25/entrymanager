package com.demo.entrymanager.service;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.exception.MissingScenarioException;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;
import com.demo.entrymanager.repository.JournalEntryRepository;
import com.demo.entrymanager.service.impl.JournalEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
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
    void givenJournalEntryWithoutScenario_whenJournalEntryIsCreated_thenThrowException(){
        //arrange
        final JournalEntryDto journalEntryDto = new JournalEntryDto(null, null, null, null, null, null, null, null);

        //assert
         assertThrows(MissingScenarioException.class,

                 //act
                 () -> journalEntryService.createJournalEntry(journalEntryDto));
    }

    @Test
    void givenJournalEntryDetails_whenJournalEntryIsCreated_thenSetsDraftStatusAndDraftedDate(){
        //arrange
        final String scenario = "test scenario";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(null, scenario, null, null, null, null, null, null);
        final JournalEntry savedJournalEntry = new JournalEntry(1L, scenario, Status.DRAFT, LocalDateTime.now(), null, null, null);
        when(journalEntryRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);
        //act
        final JournalEntryDto createdJournalEntryDto = journalEntryService.createJournalEntry(journalEntryDto);

        //asserts if the framework is working...
        assertNotNull(createdJournalEntryDto);
        assertEquals(Status.DRAFT, createdJournalEntryDto.status());
        assertNotNull(createdJournalEntryDto.draftedDate());

        //assert (the real one)
        final ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        final JournalEntry capturedEntry = captor.getValue();
        assertNotNull(capturedEntry.getScenario());
        assertEquals(scenario, capturedEntry.getScenario());
        assertEquals(Status.DRAFT, capturedEntry.getStatus());
        assertNotNull(capturedEntry.getDraftedDate());
    }

    @Test
    void givenJournalEntryDetails_whenJournalEntryIsCreated_thenCallsJournalEntryRepositorySave(){
        //arrange
        final JournalEntryDto journalEntryDto = new JournalEntryDto(
                null, "test scenario", null, null, null, null, null, null);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(new JournalEntry());

        //act
        journalEntryService.createJournalEntry(journalEntryDto);

        //assert
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
    }
}

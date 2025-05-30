package com.demo.entrymanager.service;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.exception.MissingScenarioException;
import com.demo.entrymanager.model.Accountant;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;
import com.demo.entrymanager.repository.AccountantRepository;
import com.demo.entrymanager.repository.JournalEntryRepository;
import com.demo.entrymanager.service.impl.JournalEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JournalEntryServiceTest {
    private JournalEntryService journalEntryService;
    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountantRepository accountantRepository;

    @BeforeEach
    void setup(){
        journalEntryService = new JournalEntryServiceImpl(journalEntryRepository, accountantRepository);
    }

    @Test
    void givenDraftedJournalEntry_whenAccountantIsAssigned_thenStatusIsInReview(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final JournalEntry draftedJournalEntry = new JournalEntry(journalEntryId, scenario, Status.DRAFT, LocalDateTime.now());

        final Long accountantId = 1L;
        final String accountantName = "David Marshall";
        final Accountant accountant = new Accountant(accountantId, accountantName);
        final JournalEntry savedJournalEntry = new JournalEntry(journalEntryId, scenario, Status.IN_REVIEW, LocalDateTime.now());
        savedJournalEntry.setAccountant(accountant);

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(draftedJournalEntry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(savedJournalEntry);
        when(accountantRepository.findById(accountantId)).thenReturn(Optional.of(accountant));

        final JournalEntryDto updatedJournalEntryDto = journalEntryService.assignAccountantToJournalEntry(journalEntryId, accountantId);

        //assert (I will keep this coming for now)
        assertEquals(journalEntryId, updatedJournalEntryDto.id());
        assertEquals(accountantId, accountant.getId());
        assertEquals(Status.IN_REVIEW, updatedJournalEntryDto.status());
        assertEquals(accountantName, updatedJournalEntryDto.assignedAccountant());

        //assert
        final ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        final JournalEntry capturedEntry = captor.getValue();
        assertNotNull(capturedEntry.getScenario());
        assertEquals(scenario, capturedEntry.getScenario());
        assertEquals(Status.IN_REVIEW, capturedEntry.getStatus());
        assertNotNull(capturedEntry.getDraftedDate());
        assertEquals(accountantName, capturedEntry.getAccountant().getName());
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
        final JournalEntry savedJournalEntry = new JournalEntry(1L, scenario, Status.DRAFT, LocalDateTime.now());
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

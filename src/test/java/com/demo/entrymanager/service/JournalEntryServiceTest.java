package com.demo.entrymanager.service;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.exception.*;
import com.demo.entrymanager.model.Accountant;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;
import com.demo.entrymanager.repository.AccountantRepository;
import com.demo.entrymanager.repository.JournalEntryRepository;
import com.demo.entrymanager.service.impl.JournalEntryServiceImpl;
import com.demo.entrymanager.util.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

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
    void givenReviewedJournalEntryWithApproveNotes_whenApproving_thenStatusIsApproved(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final LocalDateTime reviewedDate = draftedDate.plusDays(2);
        final LocalDateTime approvedDate = reviewedDate.plusDays(3);

        final JournalEntry reviewedJournalEntry = new JournalEntry(journalEntryId, scenario, Status.REVIEWED, draftedDate);
        final String reviewNotes = "test review notes";
        final String approveNotes = "test approve notes";
        reviewedJournalEntry.setReviewNotes(reviewNotes);
        reviewedJournalEntry.setReviewedDate(reviewedDate);
        reviewedJournalEntry.setApproveNotes(approveNotes);
        final JournalEntry approvingJournalEntry = new JournalEntry(journalEntryId, scenario, Status.APPROVED, draftedDate);
        approvingJournalEntry.setReviewNotes(reviewNotes);
        approvingJournalEntry.setReviewedDate(reviewedDate);
        approvingJournalEntry.setApprovedDate(approvedDate);
        approvingJournalEntry.setApproveNotes(approveNotes);

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(reviewedJournalEntry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(approvingJournalEntry);

        final JournalEntryDto approvedJournalEntryDto = journalEntryService.approveJournalEntry(journalEntryId);

        //assert (if the method returns)
        assertEquals(journalEntryId, approvedJournalEntryDto.id());
        assertEquals(Status.APPROVED, approvedJournalEntryDto.status());
        assertEquals(approveNotes, approvedJournalEntryDto.approveNotes());
        assertEquals(reviewNotes, approvedJournalEntryDto.reviewNotes());
        assertEquals(scenario, approvedJournalEntryDto.scenario());
        assertNotNull(approvedJournalEntryDto.draftedDate());
        assertNotNull(approvedJournalEntryDto.reviewedDate());
        assertNotNull(approvedJournalEntryDto.approvedDate());

        //assert (if save on the repo is called with updated state object)
        final ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        final JournalEntry capturedEntry = captor.getValue();
        assertNotNull(capturedEntry.getScenario());
        assertEquals(scenario, capturedEntry.getScenario());
        assertEquals(reviewNotes, capturedEntry.getReviewNotes());
        assertEquals(approveNotes, capturedEntry.getApproveNotes());
        assertEquals(Status.APPROVED, capturedEntry.getStatus());
        assertNotNull(capturedEntry.getDraftedDate());
        assertNotNull(capturedEntry.getReviewedDate());
        assertNotNull(capturedEntry.getApprovedDate());

    }


    @Test
    void givenDraftJournalEntryWithoutReviewNotes_whenReviewing_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, "test scenario", Status.IN_REVIEW, LocalDateTime.now());

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));

        //assert
        assertThrows(JournalEntryMissingReviewNotesException.class,

                //act
                () -> journalEntryService.reviewJournalEntry(journalEntryId));
    }


    @Test
    void givenJournalEntryNotInReviewState_whenReviewed_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, "test scenario", Status.DRAFT, LocalDateTime.now());

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));

        //assert
        assertThrows(InvalidJournalEntryStateException.class,

                //act
                () -> journalEntryService.reviewJournalEntry(journalEntryId));
    }

    @Test
    void givenNonexistentJournalEntry_whenReviewed_thenThrowException(){
        //arrange
        final Long nonexistentJournalEntryId = Long.MAX_VALUE;

        when(journalEntryRepository.findById(nonexistentJournalEntryId)).thenReturn(Optional.empty());

        //assert
        assertThrows(JournalEntryNotFoundException.class,

                //act
                () -> journalEntryService.reviewJournalEntry(nonexistentJournalEntryId));
    }


    @Test
    void givenJournalEntryInReviewWithReviewNotes_whenReviewed_thenStatusIsReviewed(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final JournalEntry inReviewJournalEntry = new JournalEntry(journalEntryId, scenario, Status.IN_REVIEW, LocalDateTime.now());
        final String reviewNotes = "test review notes";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final LocalDateTime reviewedDate = draftedDate.plusDays(2);
        inReviewJournalEntry.setReviewNotes(reviewNotes);
        final JournalEntry savedJournalEntry = new JournalEntry(journalEntryId, scenario, Status.REVIEWED, draftedDate);
        savedJournalEntry.setReviewedDate(reviewedDate);
        savedJournalEntry.setReviewNotes(reviewNotes);

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(inReviewJournalEntry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(savedJournalEntry);

        final JournalEntryDto reviewedJournalEntryDto = journalEntryService.reviewJournalEntry(journalEntryId);

        //assert (if the method returns)
        assertEquals(journalEntryId, reviewedJournalEntryDto.id());
        assertEquals(Status.REVIEWED, reviewedJournalEntryDto.status());
        assertEquals(reviewNotes, reviewedJournalEntryDto.reviewNotes());
        assertEquals(scenario, reviewedJournalEntryDto.scenario());
        assertNotNull(reviewedJournalEntryDto.draftedDate());
        assertNotNull(reviewedJournalEntryDto.reviewedDate());

        //assert (if save on the repo is called with updated state object)
        final ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        final JournalEntry capturedEntry = captor.getValue();
        assertNotNull(capturedEntry.getScenario());
        assertEquals(scenario, capturedEntry.getScenario());
        assertEquals(Status.REVIEWED, capturedEntry.getStatus());
        assertNotNull(capturedEntry.getReviewedDate());
        assertNotNull(capturedEntry.getDraftedDate());

    }


    @Test
    void givenJournalEntryNotInDraftState_whenAssigningAccountant_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, "test scenario", Status.IN_REVIEW, LocalDateTime.now());
        final Long accountantId = 1L;

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));

        //assert
        assertThrows(InvalidJournalEntryStateException.class,

                //act
                () -> journalEntryService.assignAccountantToJournalEntry(journalEntryId, accountantId));
    }

    @Test
    void givenNonexistentAccountant_whenAssigningAccountant_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final Long nonexistentAccountantId = Long.MAX_VALUE;
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, "test scenario", Status.DRAFT, LocalDateTime.now());

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));
        when(accountantRepository.findById(nonexistentAccountantId)).thenReturn(Optional.empty());

        //assert
        assertThrows(AccountantNotFoundException.class,

                //act
                () -> journalEntryService.assignAccountantToJournalEntry(journalEntryId, nonexistentAccountantId));
    }

    @Test
    void givenNonexistentJournalEntry_whenAssigningAccountant_thenThrowException(){
        //arrange
        final Long nonexistentJournalEntryId = Long.MAX_VALUE;
        final Long accountantId = 1L;

        when(journalEntryRepository.findById(nonexistentJournalEntryId)).thenReturn(Optional.empty());

        //assert
        assertThrows(JournalEntryNotFoundException.class,

                //act
                () -> journalEntryService.assignAccountantToJournalEntry(nonexistentJournalEntryId, accountantId));
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
        final JournalEntryDto journalEntryDto = new JournalEntryDto(null, null, null, null, null, null, null, null, null);

        //assert
         assertThrows(MissingScenarioException.class,

                 //act
                 () -> journalEntryService.createJournalEntry(journalEntryDto));
    }

    @Test
    void givenJournalEntryDetails_whenJournalEntryIsCreated_thenSetsDraftStatusAndDraftedDate(){
        //arrange
        final String scenario = "test scenario";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(null, scenario, null, null, null, null, null, null, null);
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
                null, "test scenario", null, null, null, null, null, null, null);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(new JournalEntry());

        //act
        journalEntryService.createJournalEntry(journalEntryDto);

        //assert
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
    }
}

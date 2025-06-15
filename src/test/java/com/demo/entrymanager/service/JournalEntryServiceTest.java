package com.demo.entrymanager.service;

import com.demo.entrymanager.controller.FilterJournalEntryDto;
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

import java.time.LocalDateTime;
import java.util.List;
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

    // TODO: cover other filter combination errors to fit the business rules
    @Test
    void givenFilerCriteriaWithReviewedDataBeforeDraftedDate_whenGettingJournalEntries_thenThrowException(){

        final FilterJournalEntryDto filterJournalEntryDto =
                new FilterJournalEntryDto(
                        null,
                        LocalDateTime.of(2025,5,28,0,0),
                        LocalDateTime.of(2025,4,28,0,0),
                        null);

        //assert
        final InvalidDateRangeException exception =  assertThrows(
                InvalidDateRangeException.class,

                //act
                () -> journalEntryService.getJournalEntries(filterJournalEntryDto));

        //assert
        assertEquals(ErrorMessages.INVALID_DATE_RANGE, exception.getMessage());
    }



    @Test
    void givenFilerCriteria_whenGettingJournalEntries_thenFilteredJournalEntries(){
        final FilterJournalEntryDto filterJournalEntryDto =
                new FilterJournalEntryDto(List.of(Status.DRAFT), null, null, null);
        final List<JournalEntry> filteredJournalEntries = List.of(
                new JournalEntry(1L, "test scenario 1", Status.DRAFT, LocalDateTime.now()),
                new JournalEntry(2L, "test scenario 2", Status.DRAFT, LocalDateTime.now())
        );

        when(journalEntryRepository.findWithFilters(anyList(), any(), any(), any())).thenReturn(filteredJournalEntries);

        final List<JournalEntryDto> returnedFilteredJournalEntryDtos = journalEntryService.getJournalEntries(filterJournalEntryDto);

         assertEquals(2, returnedFilteredJournalEntryDtos.size());
    }


    @Test
    void givenNonexistentJournalEntry_whenGetting_thenThrowException(){
        //arrange
        final Long nonexistentJournalEntryId = Long.MAX_VALUE;
        when(journalEntryRepository.findById(nonexistentJournalEntryId)).thenReturn(Optional.empty());

        //assert
        final JournalEntryNotFoundException exception =  assertThrows(
                JournalEntryNotFoundException.class,

                //act
                () -> journalEntryService.getJournalEntryById(nonexistentJournalEntryId));

        //assert
        assertEquals(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND, exception.getMessage());
    }


    @Test
    void givenValidJournalEntryId_whenGettingJournalEntry_thenReturnJournalEntry(){

        final long journalEntryId = 1L;
        final String testScenario = "test scenario";
        final Status draftStatus = Status.DRAFT;
        final LocalDateTime draftedDate = LocalDateTime.now();
        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(
                new JournalEntry(journalEntryId, testScenario, draftStatus, draftedDate)
        ));

        final JournalEntryDto journalEntryDto = journalEntryService.getJournalEntryById(journalEntryId);

        assertNotNull(journalEntryDto);
        assertEquals(journalEntryId, journalEntryDto.id());
        assertEquals(draftStatus, journalEntryDto.status());
        assertEquals(testScenario, journalEntryDto.scenario());
        assertNotNull(journalEntryDto.draftedDate());

    }


    // TODO: cover tests for all the fields that can be updated

    @Test
    void givenReviewedJournalEntryWithUpdatedReviewNotes_whenUpdating_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, scenario, Status.REVIEWED, draftedDate);
        final String reviewNotes = "test review notes";
        journalEntry.setReviewNotes(reviewNotes);
        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));

        final String updatedReviewNotes = "test updated review notes";
        final JournalEntryDto journalEntryDto =
                new JournalEntryDto(journalEntryId, scenario, Status.REVIEWED, draftedDate, null, null, null, updatedReviewNotes, null);

        //assert
        final InvalidJournalEntryStateException exception = assertThrows(
                InvalidJournalEntryStateException.class,

                //act
                () -> journalEntryService.updateJournalEntry(journalEntryId, journalEntryDto)
        );
        //assert
        assertEquals(ErrorMessages.REVIEW_NOTES_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_IN_REVIEW, exception.getMessage());
    }

    @Test
    void givenInReviewJournalEntryWithReviewNotes_whenUpdating_thenReviewNotesAreUpdated(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final String reviewNotes = "test review notes";
        final String updatedReviewNotes = "test updated review notes";

        final JournalEntry inreviewJournalEntry = new JournalEntry(journalEntryId, scenario, Status.IN_REVIEW, draftedDate);
        inreviewJournalEntry.setReviewNotes(reviewNotes);

        final JournalEntry updatedReviewNotesJournalEntry = new JournalEntry(journalEntryId, scenario, Status.IN_REVIEW, draftedDate);
        updatedReviewNotesJournalEntry.setReviewNotes(updatedReviewNotes);

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(inreviewJournalEntry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(updatedReviewNotesJournalEntry);

        final JournalEntryDto updatingJournalEntryDto = new JournalEntryDto(journalEntryId, scenario, Status.IN_REVIEW, draftedDate, null, null, null, updatedReviewNotes, null);

        //act
        final JournalEntryDto updatedJournalEntryDto = journalEntryService.updateJournalEntry(journalEntryId, updatingJournalEntryDto);

        //assert (if the method returns the correctly parsed dto)
        assertEquals(journalEntryId, updatedJournalEntryDto.id());
        assertEquals(Status.IN_REVIEW, updatedJournalEntryDto.status());
        assertEquals(scenario, updatedJournalEntryDto.scenario());
        assertNotNull(updatedJournalEntryDto.draftedDate());
        assertEquals(updatedReviewNotes, updatedJournalEntryDto.reviewNotes());

        //assert (if save on the repo is called with updated state object)
        final ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        final JournalEntry capturedEntry = captor.getValue();
        assertEquals(journalEntryId, capturedEntry.getId());
        assertEquals(Status.IN_REVIEW, capturedEntry.getStatus());
        assertEquals(scenario, capturedEntry.getScenario());
        assertNotNull(capturedEntry.getDraftedDate());
        assertEquals(updatedReviewNotes, capturedEntry.getReviewNotes());
    }
    //

    @Test
    void givenApprovedJournalEntryWithUpdatedScenario_whenUpdating_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final String testUpdatedScenario = "test scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, testUpdatedScenario, Status.APPROVED, draftedDate);
        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));

        final String updatedScenario = "test updated scenario";
        final JournalEntryDto journalEntryDto =
                new JournalEntryDto(journalEntryId, updatedScenario, Status.APPROVED, draftedDate, null, null, null, null, null);

        //assert
        final InvalidJournalEntryStateException exception = assertThrows(
                InvalidJournalEntryStateException.class,

                //act
                () -> journalEntryService.updateJournalEntry(journalEntryId, journalEntryDto)
        );
        //assert
        assertEquals(ErrorMessages.SCENARIO_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_DRAFT, exception.getMessage());
    }

    @Test
    void givenReviewedJournalEntryWithUpdatedScenario_whenUpdating_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, scenario, Status.REVIEWED, draftedDate);
        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));

        final String updatedScenario = "test updated scenario";
        final JournalEntryDto journalEntryDto =
                new JournalEntryDto(journalEntryId, updatedScenario, Status.REVIEWED, draftedDate, null, null, null, null, null);

        //assert
        final InvalidJournalEntryStateException exception = assertThrows(
                InvalidJournalEntryStateException.class,

                //act
                () -> journalEntryService.updateJournalEntry(journalEntryId, journalEntryDto)
        );
        //assert
        assertEquals(ErrorMessages.SCENARIO_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_DRAFT, exception.getMessage());
    }

    @Test
    void givenInReviewJournalEntryWithUpdatedScenario_whenUpdating_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final JournalEntry journalEntry = new JournalEntry(journalEntryId, scenario, Status.IN_REVIEW, draftedDate);
        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(journalEntry));

        final String testUpdatedScenario = "test updated scenario";
        final JournalEntryDto journalEntryDto =
                new JournalEntryDto(journalEntryId, testUpdatedScenario, Status.IN_REVIEW, draftedDate, null, null, null, null, null);


        //assert
        final InvalidJournalEntryStateException exception = assertThrows(
                InvalidJournalEntryStateException.class,

                //act
                () -> journalEntryService.updateJournalEntry(journalEntryId, journalEntryDto)
        );
        //assert
        assertEquals(ErrorMessages.SCENARIO_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_DRAFT, exception.getMessage());
    }

    @Test
    void givenNonexistentJournalEntry_whenUpdating_thenThrowException(){
        //arrange
        final Long nonexistentJournalEntryId = Long.MAX_VALUE;
        final JournalEntryDto nonexistentJournalEntryDto =
                new JournalEntryDto(nonexistentJournalEntryId, "test scenario", Status.DRAFT, LocalDateTime.now(), null, null, null, null, null);

        when(journalEntryRepository.findById(nonexistentJournalEntryId)).thenReturn(Optional.empty());

        //assert
        final JournalEntryNotFoundException exception =  assertThrows(
                JournalEntryNotFoundException.class,

                //act
                () -> journalEntryService.updateJournalEntry(nonexistentJournalEntryId, nonexistentJournalEntryDto));

        //assert
        assertEquals(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND, exception.getMessage());
    }


    @Test
    void givenDraftedJournalEntryWithScenario_whenUpdating_thenScenarioIdUpdated(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final String updatedScenario = "test updated scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();

        final JournalEntry draftedJournalEntry = new JournalEntry(journalEntryId, scenario, Status.DRAFT, draftedDate);

        final JournalEntry updatedScenarioJournalEntry = new JournalEntry(journalEntryId, updatedScenario, Status.DRAFT, draftedDate);

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(draftedJournalEntry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(updatedScenarioJournalEntry);

        final JournalEntryDto updatingJournalEntryDto = new JournalEntryDto(journalEntryId, updatedScenario, Status.DRAFT, draftedDate, null, null, null, null, null);
        //act
        final JournalEntryDto updatedJournalEntryDto = journalEntryService.updateJournalEntry(journalEntryId, updatingJournalEntryDto);

        //assert (if the method returns the correctly parsed dto)
        assertEquals(journalEntryId, updatedJournalEntryDto.id());
        assertEquals(Status.DRAFT, updatedJournalEntryDto.status());
        assertEquals(updatedScenario, updatedJournalEntryDto.scenario());
        assertNotNull(updatedJournalEntryDto.draftedDate());

        //assert (if save on the repo is called with updated state object)
        final ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        final JournalEntry capturedEntry = captor.getValue();
        assertEquals(journalEntryId, capturedEntry.getId());
        assertEquals(Status.DRAFT, capturedEntry.getStatus());
        assertEquals(updatedScenario, capturedEntry.getScenario());
        assertNotNull(capturedEntry.getDraftedDate());
    }


    @Test
    void givenJournalEntryNotInReviewedState_whenApproving_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final JournalEntry notInReviewedStateJournalEntry =
                new JournalEntry(journalEntryId, "test scenario", Status.IN_REVIEW, LocalDateTime.now());
        notInReviewedStateJournalEntry.setReviewNotes("test review notes");
        notInReviewedStateJournalEntry.setApproveNotes("test approval notes");
        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(notInReviewedStateJournalEntry));

        //assert
        assertThrows(InvalidJournalEntryStateException.class,

                //act
                () -> journalEntryService.approveJournalEntry(journalEntryId));
    }


    @Test
    void givenReviewedJournalEntryWithoutApproveNots_whenApproving_thenThrowException(){
        //arrange
        final Long journalEntryId = 1L;
        final String scenario = "test scenario";
        final LocalDateTime draftedDate = LocalDateTime.now();
        final LocalDateTime reviewedDate = draftedDate.plusDays(2);

        final JournalEntry missingApprovalNotesJournalEntry = new JournalEntry(journalEntryId, scenario, Status.REVIEWED, draftedDate);
        final String reviewNotes = "test review notes";
        missingApprovalNotesJournalEntry.setReviewNotes(reviewNotes);
        missingApprovalNotesJournalEntry.setReviewedDate(reviewedDate);

        when(journalEntryRepository.findById(journalEntryId)).thenReturn(Optional.of(missingApprovalNotesJournalEntry));

        //assert
        assertThrows(JournalEntryMissingApproveNotesException.class,

                //act
                () -> journalEntryService.approveJournalEntry(journalEntryId));
    }

    @Test
    void givenNonexistentJournalEntry_whenApproving_thenThrowException(){
        //arrange
        final Long nonexistentJournalEntryId = Long.MAX_VALUE;

        when(journalEntryRepository.findById(nonexistentJournalEntryId)).thenReturn(Optional.empty());

        //assert
        assertThrows(JournalEntryNotFoundException.class,

                //act
                () -> journalEntryService.approveJournalEntry(nonexistentJournalEntryId));
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
        savedJournalEntry.setAssignedAccountant(accountant);

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
        assertEquals(accountantName, capturedEntry.getAssignedAccountant().getName());
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

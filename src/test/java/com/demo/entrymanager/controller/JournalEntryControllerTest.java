package com.demo.entrymanager.controller;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.exception.*;
import com.demo.entrymanager.model.Status;
import com.demo.entrymanager.service.JournalEntryService;
import com.demo.entrymanager.util.ErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JournalEntryController.class)
public class JournalEntryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JournalEntryService journalEntryService;


    @Test
    void givenInvalidDateRange_whenGettingJournalEntry_thenThrowException() throws Exception{
        //arrange
        when(journalEntryService.getJournalEntries(any(FilterJournalEntryDto.class)))
                .thenThrow(new InvalidDateRangeException(ErrorMessages.INVALID_DATE_RANGE));

        //act
        mockMvc.perform(
                        get("/api/v1/journalentries")
                                .param("draftedDate", LocalDateTime.now().toString())
                                .param("reviewedDate", LocalDateTime.now().minusDays(3).toString()))

                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.INVALID_DATE_RANGE));
    }


    @Test
    void givenFilterCriteria_whenGettingJournalEntries_thenReturnFilteredJournalEntries() throws Exception{
        //arrange
        final String accountantName = "David Marshall";
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final JournalEntryDto journalEntryDto1 = new JournalEntryDto(1L, journalEntryScenario, Status.DRAFT, LocalDateTime.now(), null, null, accountantName, null, null);
        final JournalEntryDto journalEntryDto2 = new JournalEntryDto(2L, journalEntryScenario, Status.DRAFT, LocalDateTime.now().minusDays(2), null, null, accountantName, null, null);
        final List<JournalEntryDto> filteredJournalEntries = List.of(journalEntryDto1, journalEntryDto2);
        when(journalEntryService.getJournalEntries(any(FilterJournalEntryDto.class)))
                .thenReturn(filteredJournalEntries);
        //act
        mockMvc.perform(
                        get("/api/v1/journalentries")
                                .param("status", "DRAFT,IN_REVIEW")
                                .param("draftedDate", LocalDateTime.now().minusDays(3).toString())
                                .param("reviewedDate", LocalDateTime.now().toString())
                                .param("assignedAccountant", accountantName))

                //assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(filteredJournalEntries.size())))
                .andExpect(jsonPath("$[0].id").value(journalEntryDto1.id()))
                .andExpect(jsonPath("$[1].id").value(journalEntryDto2.id()));
    }

    @Test
    void givenInvalidJournalEntryId_whenGettingJournalEntry_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = Long.MAX_VALUE;
        when(journalEntryService.getJournalEntryById(journalEntryId))
                .thenThrow(new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));

        //act
        mockMvc.perform(
                        get("/api/v1/journalentries/{id}", journalEntryId)
                                .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
    }

    @Test
    void givenValidJournalEntryId_whenGettingJournalEntry_thenReturnJournalEntryDetails() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(journalEntryId, journalEntryScenario, Status.DRAFT, null, null, null, null, null, null);
        when(journalEntryService.getJournalEntryById(journalEntryId))
                .thenReturn(journalEntryDto);
        //act
        mockMvc.perform(
                        get("/api/v1/journalentries/{id}", journalEntryId)
                                .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(journalEntryId));
    }

    @Test
    @DisplayName("Given a Journal Entry is approved, when updating it, InvalidJournalEntryStateException is thrown.")
    void givenApprovedJournalEntry_whenUpdating_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(journalEntryId, journalEntryScenario, Status.DRAFT, null, null, null, null, null, null);
        when(journalEntryService.updateJournalEntry(journalEntryId, journalEntryDto))
                .thenThrow(new InvalidJournalEntryStateException(ErrorMessages.APPROVED_JOURNAL_ENTRIES_CAN_NOT_BE_UPDATED));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journalEntryDto)))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.APPROVED_JOURNAL_ENTRIES_CAN_NOT_BE_UPDATED));
    }

    @Test
    void givenJournalEntryNonexistent_whenJournalEntryDetailsAreUpdating_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(journalEntryId, journalEntryScenario, Status.DRAFT, null, null, null, null, null, null);
        when(journalEntryService.updateJournalEntry(eq(journalEntryId), any(JournalEntryDto.class)))
                .thenThrow(new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
        //act
        mockMvc.perform(
                        put("/api/v1/journalentries/{id}",journalEntryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(journalEntryDto)))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
    }

    @Test
    void givenJournalEntryDetails_whenJournalEntryDetailsAreUpdating_thenJournalEntryDetailsAreUpdated() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(journalEntryId, journalEntryScenario, Status.DRAFT, null, null, null, null, null, null);
        when(journalEntryService.updateJournalEntry(eq(journalEntryId), any(JournalEntryDto.class)))
                .thenReturn(journalEntryDto);
        //act
        mockMvc.perform(
                        put("/api/v1/journalentries/{id}",journalEntryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(journalEntryDto)))
        //assert
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scenario").value(journalEntryScenario));
    }



    @Test
    void givenDraftJournalEntry_whenApproving_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        when(journalEntryService.approveJournalEntry(journalEntryId))
                .thenThrow(new InvalidJournalEntryStateException(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_REVIEWED_CAN_BE_APPROVED));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/approve", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_REVIEWED_CAN_BE_APPROVED));
    }


    @Test
    void givenReviewedJournalEntryWithoutApproveNotes_whenApproving_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        when(journalEntryService.approveJournalEntry(journalEntryId))
                .thenThrow(new JournalEntryMissingApproveNotesException(ErrorMessages.APPROVE_NOTES_REQUIRED));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/approve", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON))
        //assert
        .andExpect(status().isBadRequest())
        .andExpect(content().string(ErrorMessages.APPROVE_NOTES_REQUIRED));
    }

    @Test
    void givenJournalEntryNonexistent_whenApproving_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = Long.MAX_VALUE;
        when(journalEntryService.approveJournalEntry(journalEntryId))
                .thenThrow(new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/approve", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
    }

    @Test
    void givenReviewedJournalEntryWithApproveNotes_whenApproving_thenStatusIsApproved() throws Exception{
        final String accountantName = "David Marshall";
        final Long journalEntryId = 1L;
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final String reviewNotes = "reviewed without comments";
        final String approveNotes = "accepted without comments";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(
                journalEntryId, journalEntryScenario, Status.APPROVED, null, null, null, accountantName, reviewNotes, approveNotes);
        when(journalEntryService.approveJournalEntry(journalEntryId))
                .thenReturn(journalEntryDto);

        mockMvc.perform(put("/api/v1/journalentries/{id}/approve", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.APPROVED.name()));
    }

    @Test
    void givenDraftJournalEntryWithoutReviewNotes_whenReviewing_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = 1L;
        when(journalEntryService.reviewJournalEntry(journalEntryId))
                .thenThrow(new JournalEntryMissingReviewNotesException(ErrorMessages.REVIEW_NOTES_REQUIRED));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/review", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON))
        //assert
        .andExpect(status().isBadRequest())
        .andExpect(content().string(ErrorMessages.REVIEW_NOTES_REQUIRED));
    }

    @Test
    void givenJournalEntryNonexistent_whenReviewing_thenThrowException() throws Exception{
        //arrange
        final Long journalEntryId = Long.MAX_VALUE;
        when(journalEntryService.reviewJournalEntry(journalEntryId))
                .thenThrow(new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/review", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
    }

    @Test
    void givenJournalEntryInReview_whenReviewing_thenStatusIsReviewed() throws Exception{
        final String accountantName = "David Marshall";
        final Long journalEntryId = 1L;
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final String reviewNotes = "reviewed without comments";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(
                journalEntryId, journalEntryScenario, Status.REVIEWED, null, null, null, accountantName, reviewNotes, null);

        when(journalEntryService.reviewJournalEntry(journalEntryId))
                .thenReturn(journalEntryDto);

        mockMvc.perform(put("/api/v1/journalentries/{id}/review", journalEntryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.REVIEWED.name()));
    }

    @Test
    void givenJournalEntryNonexistent_whenAssigningAccountant_thenThrowException() throws Exception{
        //arrange
        final Long accountantId = 1L;
        final Long journalEntryId = Long.MAX_VALUE;
        when(journalEntryService.assignAccountantToJournalEntry(journalEntryId, accountantId))
                .thenThrow(new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/accountant/{accountantId}", journalEntryId, accountantId)
                        .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
    }

    @Test
    void givenAccountantNonexistent_whenAssigningToJournalEntry_thenThrowException() throws Exception{
        //arrange
        final Long accountantId = Long.MAX_VALUE;
        final Long journalEntryId = 1L;
        when(journalEntryService.assignAccountantToJournalEntry(journalEntryId, accountantId))
                .thenThrow(new AccountantNotFoundException(ErrorMessages.ACCOUNTANT_NOT_FOUND));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/accountant/{accountantId}", journalEntryId, accountantId)
                        .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ACCOUNTANT_NOT_FOUND));
    }


    @Test
    void givenJournalEntryNotInDraftState_whenAssigningAccountant_thenThrowException() throws Exception{
        //arrange
        final Long accountantId = 1L;
        final Long journalEntryId = 1L;
        when(journalEntryService.assignAccountantToJournalEntry(journalEntryId, accountantId))
                .thenThrow(new InvalidJournalEntryStateException(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_DRAFT_CAN_BE_ASSIGNED_TO_AN_ACCOUNTANT));
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/accountant/{accountantId}", journalEntryId, accountantId)
                        .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_DRAFT_CAN_BE_ASSIGNED_TO_AN_ACCOUNTANT));
    }



    @Test
    void givenDraftOfJournalEntry_whenAssigningAccountant_thenStatusIsInReview() throws Exception{
        //arrange
        final Long accountantId = 1L;
        final String accountantName = "David Marshall";
        final Long journalEntryId = 1L;
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(
                journalEntryId, journalEntryScenario, Status.IN_REVIEW, null, null, null, accountantName, null, null);
        when(journalEntryService.assignAccountantToJournalEntry(journalEntryId, accountantId))
                .thenReturn(journalEntryDto);
        //act
        mockMvc.perform(put("/api/v1/journalentries/{id}/accountant/{accountantId}", journalEntryId, accountantId)
                        .contentType(MediaType.APPLICATION_JSON))
                //assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.IN_REVIEW.name()))
                .andExpect(jsonPath("$.assignedAccountant").value(accountantName));
    }

    @Test
    void givenJournalEntryWithoutScenario_whenJournalEntryIsCreated_thenThrowException() throws Exception{
        //arrange
        final JournalEntryDto journalEntryDto = new JournalEntryDto(null, null, Status.DRAFT, null, null, null, null, null, null);
        when(journalEntryService.createJournalEntry(any(JournalEntryDto.class)))
                .thenThrow(new MissingScenarioException(ErrorMessages.SCENARIO_MISSING));
        //act
        mockMvc.perform(
                        post("/api/v1/journalentries")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(journalEntryDto)))
                //assert
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.SCENARIO_MISSING));
    }


    @Test
    void givenJournalEntryDetails_whenJournalEntryIsCreated_thenJournalEntrySaved() throws Exception{
        //arrange
        final String journalEntryScenario = "On 27 May 2025, company XX purchased office supplies for $500 in cash.";
        final JournalEntryDto journalEntryDto = new JournalEntryDto(null, journalEntryScenario, Status.DRAFT, null, null, null, null, null, null);
        when(journalEntryService.createJournalEntry(any(JournalEntryDto.class)))
                .thenReturn(journalEntryDto);
        //act
        mockMvc.perform(
                post("/api/v1/journalentries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journalEntryDto)))
                //assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scenario").value(journalEntryScenario))
                .andExpect(jsonPath("$.status").value(Status.DRAFT.name()));
    }
}

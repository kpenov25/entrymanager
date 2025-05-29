package com.demo.entrymanager.service;

import com.demo.entrymanager.controller.FilterJournalEntryDto;
import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.exception.*;

import java.util.List;

public interface JournalEntryService {

    /**
     * Creates a new journal entry.
     *
     * @param journalEntryDto data to create the journal entry.
     * @return the created journalEntryDto.
     * @throws MissingScenarioException is the scenario is missing the data provided to create the journal entry.
     */
    JournalEntryDto createJournalEntry(JournalEntryDto journalEntryDto);

    /**
     * Assign an accountant to a journal entry.
     *
     * @param journalEntryId the ID of the journal entry to which the accountant is to be assigned.
     * @param accountantId the ID of the accountant to be assigned.
     * @return the updated journalEntryDto with the accountant assigned.
     * @throws InvalidJournalEntryStateException if the journal entry is not in the DRAFT state.
     * @throws JournalEntryNotFoundException if the journal entry with the provided ID is not found.
     * @throws AccountantNotFoundException is the accountant with the provided ID is not found.
     */
    JournalEntryDto assignAccountantToJournalEntry(Long journalEntryId, Long accountantId);

    /**
     * Reviews a journal entry.
     *
     * @param journalEntryId the id of the journal entry to be reviewed.
     * @return the updated journalEntryDto marked as REVIEWED.
     * @throws JournalEntryMissingReviewNotesException if the journal entry is missing the reviewers notes.
     * @throws JournalEntryNotFoundException if the journal entry with the provided ID is not found.
     */
    JournalEntryDto reviewJournalEntry(Long journalEntryId);

    /**
     * Approves a journal entry.
     *
     * @param journalEntryId the id of the journal entry to be approved.
     * @return the updated journalEntryDto marked as APPROVED.
     * @throws JournalEntryMissingApproveNotesException if the journal entry is missing the approvers notes.
     * @throws JournalEntryNotFoundException if the journal entry with the provided ID is not found.
     * @throws InvalidJournalEntryStateException if the journal entry is not in the REVIEWED state.
     */
    JournalEntryDto approveJournalEntry(Long journalEntryId);

    /**
     * Update an existing journal entry (only the .scenario details are updated at this point).
     *
     * @param journalEntryId the id of the journal entry to be updated.
     * @param journalEntryDto the data object containing the updated journal entry information.
     * @return the updated journalEntryDto.
     * @throws JournalEntryNotFoundException if the journal entry with the provided ID is not found.
     * @throws InvalidJournalEntryStateException if the journal entry is in the APPROVED state, the .scenario cannot be updated.
     */
    JournalEntryDto updateJournalEntry(Long journalEntryId, JournalEntryDto journalEntryDto);

    /**
     * Retrieves a journal entry by its ID.
     *
     * @param journalEntryId the id of the journal entry to be retrieved.
     * @return the journalEntryDto corresponding to the provided ID.
     * @throws JournalEntryNotFoundException if the journal entry with the provided ID is not found.
     */
    JournalEntryDto getJournalEntryById(Long journalEntryId);

    /**
     * Retrieves a list of journal entries based on the provided filter requirements.
     *
     * @param filterJournalEntryDto the data containing the filter requirements.
     * @return a list of JournalEntryDto objects matching the filter requirements.
     * @throws InvalidDateRangeException if the drafted date of the filter is after the reviewed date.
     */
    List<JournalEntryDto> getJournalEntries(FilterJournalEntryDto filterJournalEntryDto);
}

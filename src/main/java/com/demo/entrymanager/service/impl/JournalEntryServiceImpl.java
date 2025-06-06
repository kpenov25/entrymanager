package com.demo.entrymanager.service.impl;

import com.demo.entrymanager.controller.FilterJournalEntryDto;
import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.exception.*;
import com.demo.entrymanager.model.Accountant;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;
import com.demo.entrymanager.repository.AccountantRepository;
import com.demo.entrymanager.repository.JournalEntryRepository;
import com.demo.entrymanager.service.JournalEntryService;
import com.demo.entrymanager.util.ErrorMessages;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JournalEntryServiceImpl implements JournalEntryService {
    private JournalEntryRepository journalEntryRepository;
    private AccountantRepository accountantRepository;

    public JournalEntryServiceImpl(JournalEntryRepository journalEntryRepository, AccountantRepository accountantRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountantRepository = accountantRepository;
    }

    @Override
    public JournalEntryDto createJournalEntry(final JournalEntryDto journalEntryDto) {
        if(journalEntryDto.scenario() == null || journalEntryDto.scenario().isBlank()){
            throw  new MissingScenarioException(ErrorMessages.SCENARIO_MISSING);
        }

        final JournalEntry newJournalEntry = new JournalEntry();
        newJournalEntry.setScenario(journalEntryDto.scenario());
        newJournalEntry.setStatus(Status.DRAFT);
        newJournalEntry.setDraftedDate(LocalDateTime.now());

        final JournalEntry savedJournalEntry = journalEntryRepository.save(newJournalEntry);

        return toJournalEntryDto(savedJournalEntry);
    }

    @Override
    public JournalEntryDto assignAccountantToJournalEntry(final Long journalEntryId, final Long accountantId) {
        final JournalEntry journalEntry = getJournalEntry(journalEntryId);

        if(journalEntry.getStatus() != Status.DRAFT){
            throw new InvalidJournalEntryStateException(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_DRAFT_CAN_BE_ASSIGNED_TO_AN_ACCOUNTANT);
        }

        final Accountant accountant = accountantRepository.findById(accountantId)
                .orElseThrow(()->new AccountantNotFoundException(ErrorMessages.ACCOUNTANT_NOT_FOUND));

        journalEntry.setStatus(Status.IN_REVIEW);
        journalEntry.setAccountant(accountant);

        final JournalEntry savedJournalEntry = journalEntryRepository.save(journalEntry);

        return toJournalEntryDto(savedJournalEntry);
    }

    @Override
    public JournalEntryDto reviewJournalEntry(final Long journalEntryId) {
        final JournalEntry journalEntry = getJournalEntry(journalEntryId);

        validateJournalEntryBeforeReviewing(journalEntry);

        journalEntry.setStatus(Status.REVIEWED);
        journalEntry.setReviewedDate(LocalDateTime.now());

        final JournalEntry savedJournalEntry = journalEntryRepository.save(journalEntry);

        return toJournalEntryDto(savedJournalEntry);
    }

    @Override
    public JournalEntryDto approveJournalEntry(final Long journalEntryId) {
        final JournalEntry journalEntry = getJournalEntry(journalEntryId);

        validateJournalEntryBeforeApproval(journalEntry);

        journalEntry.setStatus(Status.APPROVED);
        journalEntry.setApprovedDate(LocalDateTime.now());

        final JournalEntry approvedJournalEntry = journalEntryRepository.save(journalEntry);
        return toJournalEntryDto(approvedJournalEntry);
    }

    @Override
    public JournalEntryDto updateJournalEntry(final Long journalEntryId, final JournalEntryDto journalEntryDto) {
        final JournalEntry journalEntry = getJournalEntry(journalEntryId);
        final Status status = journalEntry.getStatus();

        if(status == Status.DRAFT){
            if(requiresUpdate(journalEntry.getReviewNotes(), journalEntryDto.reviewNotes())){
                throw new InvalidJournalEntryStateException(ErrorMessages.REVIEW_NOTES_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_IN_REVIEW);
            }

            journalEntry.setScenario(journalEntryDto.scenario());
        }else if(status == Status.IN_REVIEW){
            if(requiresUpdate(journalEntry.getScenario(), journalEntryDto.scenario())){
                throw new InvalidJournalEntryStateException(ErrorMessages.SCENARIO_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_DRAFT);
            }

            journalEntry.setReviewNotes(journalEntryDto.reviewNotes());
        }else if(status == Status.REVIEWED){
            if(requiresUpdate(journalEntry.getScenario(), journalEntryDto.scenario())){
                throw new InvalidJournalEntryStateException(ErrorMessages.SCENARIO_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_DRAFT);
            }
            if(requiresUpdate(journalEntry.getReviewNotes(), journalEntryDto.reviewNotes())){
                throw new InvalidJournalEntryStateException(ErrorMessages.REVIEW_NOTES_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_IN_REVIEW);
            }

        }else if(status == Status.APPROVED){
            if(requiresUpdate(journalEntry.getScenario(), journalEntryDto.scenario())){
                throw new InvalidJournalEntryStateException(ErrorMessages.SCENARIO_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_DRAFT);
            }
            if(requiresUpdate(journalEntry.getReviewNotes(), journalEntryDto.reviewNotes())){
                throw new InvalidJournalEntryStateException(ErrorMessages.REVIEW_NOTES_CAN_ONLY_BE_UPDATED_WHEN_JOURNAL_ENTRY_STATUS_IS_IN_REVIEW);
            }


        }


        final JournalEntry updatedJournalEntry = journalEntryRepository.save(journalEntry);

        return toJournalEntryDto(updatedJournalEntry);
    }

    private boolean requiresUpdate(String oldVal, String newVal) {
        if (oldVal == null && newVal == null) {
            return false;
        }
        if (oldVal == null || newVal == null) {
            return true;
        }
        return !oldVal.equals(newVal);
    }

    @Override
    public JournalEntryDto getJournalEntryById(final Long journalEntryId) {
        final JournalEntry journalEntry = getJournalEntry(journalEntryId);

        return toJournalEntryDto(journalEntry);
    }


    @Override
    public List<JournalEntryDto> getJournalEntries(final FilterJournalEntryDto filterJournalEntryDto) {
        if(filterJournalEntryDto.draftedDate() != null
                && filterJournalEntryDto.reviewedDate() != null
        && filterJournalEntryDto.draftedDate().isAfter(filterJournalEntryDto.reviewedDate())){
                throw new InvalidDateRangeException(ErrorMessages.INVALID_DATE_RANGE);
        }
        final List<JournalEntry> filteredJournalEntries = journalEntryRepository.findWithFilters(
                filterJournalEntryDto.status(),
                filterJournalEntryDto.draftedDate(),
                filterJournalEntryDto.reviewedDate(),
                filterJournalEntryDto.assignedAccountant()
        );

        return filteredJournalEntries
                .stream()
                .map(this::toJournalEntryDto)
                .collect(Collectors.toList());
    }

    private JournalEntry getJournalEntry(final Long journalEntryId) {
        return journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
    }

    private JournalEntryDto toJournalEntryDto(final JournalEntry journalEntry){
        return new JournalEntryDto(
                journalEntry.getId(),
                journalEntry.getScenario(),
                journalEntry.getStatus(),
                journalEntry.getDraftedDate(),
                journalEntry.getReviewedDate(),
                journalEntry.getApprovedDate(),
                journalEntry.getAccountant() != null ? journalEntry.getAccountant().getName() : null,
                journalEntry.getReviewNotes(),
                journalEntry.getApproveNotes()
        );
    }

    private void validateJournalEntryBeforeReviewing(final JournalEntry journalEntry) {
        if(journalEntry.getStatus() != Status.IN_REVIEW){
            throw new InvalidJournalEntryStateException(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_IN_REVIEW_CAN_BE_REVIEWED);
        }
        if (journalEntry.getReviewNotes() == null || journalEntry.getReviewNotes().isBlank()){
            throw new JournalEntryMissingReviewNotesException(ErrorMessages.REVIEW_NOTES_REQUIRED);
        }
    }

    private void validateJournalEntryBeforeApproval(final JournalEntry journalEntry) {
        if(journalEntry.getApproveNotes() == null || journalEntry.getApproveNotes().isBlank()){
            throw new JournalEntryMissingApproveNotesException(ErrorMessages.APPROVE_NOTES_REQUIRED);
        }
        if(journalEntry.getStatus() != Status.REVIEWED){
            throw new InvalidJournalEntryStateException(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_REVIEWED_CAN_BE_APPROVED);
        }
    }

}

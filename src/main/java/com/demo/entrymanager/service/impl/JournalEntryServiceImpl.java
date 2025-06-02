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
import java.util.Optional;

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
        final JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(()->new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));
        if(journalEntry.getStatus() == null || journalEntry.getStatus() != Status.DRAFT){
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
        final JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(()->new JournalEntryNotFoundException(ErrorMessages.JOURNAL_ENTRY_NOT_FOUND));

        if(journalEntry.getStatus() == null || journalEntry.getStatus() != Status.IN_REVIEW){
            throw new InvalidJournalEntryStateException(ErrorMessages.ONLY_JOURNAL_ENTRY_WITH_STATUS_IN_REVIEW_CAN_BE_REVIEWED);
        }
        if (journalEntry.getReviewNotes() == null || journalEntry.getReviewNotes().isBlank()){
            throw new JournalEntryMissingReviewNotesException(ErrorMessages.REVIEW_NOTES_REQUIRED);
        }

        journalEntry.setStatus(Status.REVIEWED);
        journalEntry.setReviewedDate(LocalDateTime.now());

        final JournalEntry savedJournalEntry = journalEntryRepository.save(journalEntry);

        return toJournalEntryDto(savedJournalEntry);
    }

    @Override
    public JournalEntryDto approveJournalEntry(final Long journalEntryId) {
        final JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId).get();

        journalEntry.setStatus(Status.APPROVED);
        journalEntry.setApprovedDate(LocalDateTime.now());

        final JournalEntry approvedJournalEntry = journalEntryRepository.save(journalEntry);
        return toJournalEntryDto(approvedJournalEntry);
    }

    @Override
    public JournalEntryDto updateJournalEntry(Long journalEntryId, JournalEntryDto journalEntryDto) {
        return null;
    }

    @Override
    public JournalEntryDto getJournalEntryById(Long journalEntryId) {
        return null;
    }

    @Override
    public List<JournalEntryDto> getJournalEntries(FilterJournalEntryDto filterJournalEntryDto) {
        return null;
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
}

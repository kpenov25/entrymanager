package com.demo.entrymanager.service.impl;

import com.demo.entrymanager.controller.FilterJournalEntryDto;
import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.exception.MissingScenarioException;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.repository.JournalEntryRepository;
import com.demo.entrymanager.service.JournalEntryService;
import com.demo.entrymanager.util.ErrorMessages;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalEntryServiceImpl implements JournalEntryService {
    private JournalEntryRepository journalEntryRepository;
    public JournalEntryServiceImpl(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public JournalEntryDto createJournalEntry(JournalEntryDto journalEntryDto) {
        if(journalEntryDto.scenario() == null || journalEntryDto.scenario().isBlank()){
            throw  new MissingScenarioException(ErrorMessages.SCENARIO_MISSING);
        }

        final JournalEntry newJournalEntry = new JournalEntry();
        newJournalEntry.setScenario(journalEntryDto.scenario());
        newJournalEntry.setStatus(journalEntryDto.status());
        newJournalEntry.setDraftedDate(journalEntryDto.draftedDate());

        final JournalEntry savedJournalEntry = journalEntryRepository.save(newJournalEntry);

        return new JournalEntryDto(
                savedJournalEntry.getId(),
                savedJournalEntry.getScenario(),
                savedJournalEntry.getStatus(),
                savedJournalEntry.getDraftedDate(),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public JournalEntryDto assignAccountantToJournalEntry(Long journalEntryId, Long accountantId) {
        return null;
    }

    @Override
    public JournalEntryDto reviewJournalEntry(Long journalEntryId) {
        return null;
    }

    @Override
    public JournalEntryDto approveJournalEntry(Long journalEntryId) {
        return null;
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
}

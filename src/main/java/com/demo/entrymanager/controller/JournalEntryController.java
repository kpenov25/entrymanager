package com.demo.entrymanager.controller;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.service.JournalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/journalentries")
public class JournalEntryController {
    @Autowired
    private JournalEntryService journalEntryService;

    @PostMapping
    public ResponseEntity<JournalEntryDto> createJournalEntry(@RequestBody final JournalEntryDto journalEntryDto){

        final JournalEntryDto createdJournalEntryDto = journalEntryService.createJournalEntry(journalEntryDto);

        return new ResponseEntity<>(createdJournalEntryDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/accountant/{accountantId}")
    public ResponseEntity<JournalEntryDto> assignAccountant(@PathVariable final Long id, @PathVariable final Long accountantId) {
        final JournalEntryDto journalEntryDto = journalEntryService.assignAccountantToJournalEntry(id, accountantId);
        return new ResponseEntity<>(journalEntryDto, HttpStatus.OK);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<JournalEntryDto> reviewJournalEntry(@PathVariable final Long id) {
        final JournalEntryDto journalEntryDto = journalEntryService.reviewJournalEntry(id);
        return new ResponseEntity<>(journalEntryDto, HttpStatus.OK);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<JournalEntryDto> approveJournalEntry(@PathVariable final Long id) {
        final JournalEntryDto journalEntryDto = journalEntryService.approveJournalEntry(id);
        return new ResponseEntity<>(journalEntryDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryDto> updateJournalEntry(@PathVariable final Long id, @RequestBody final JournalEntryDto journalEntryDto) {
        final JournalEntryDto updatedJournalEntryDto = journalEntryService.updateJournalEntry(id, journalEntryDto);
        return new ResponseEntity<>(updatedJournalEntryDto, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntryDto> getJournalEntry(@PathVariable final Long id) {
        final JournalEntryDto journalEntryDto = journalEntryService.getJournalEntryById(id);
        return new ResponseEntity<>(journalEntryDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<JournalEntryDto>> getJournalEntries(final FilterJournalEntryDto filterJournalEntryDto) {
        final List<JournalEntryDto> filteredJournalEntries = journalEntryService.getJournalEntries(filterJournalEntryDto);
        return new ResponseEntity<>(filteredJournalEntries, HttpStatus.OK);
    }

}

package com.demo.entrymanager.repository;

import com.demo.entrymanager.dto.JournalEntryDto;
import com.demo.entrymanager.model.JournalEntry;
import com.demo.entrymanager.model.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Sql({"/filterTestData.sql"})
public class JournalEntryRepositoryTest {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Test
    void givenStatus_whenGettingJournalEntries_thenEntriesWithMatchingStatusAreReturned(){

        List<JournalEntry> inReviewJournalEntries =
                journalEntryRepository.findWithFilters(List.of(Status.IN_REVIEW), null, null,null);

        assertNotNull(inReviewJournalEntries);
        assertEquals(1, inReviewJournalEntries.size());
    }

    @Test
    void givenDateRange_whenGettingJournalEntries_thenEntriesWithMatchingDateRangeAreReturned(){
        final LocalDateTime now = LocalDateTime.now();
        List<JournalEntry> last2DaysJournalEntries =
                journalEntryRepository.findWithFilters(null,
                        now.minusDays(2),
                        now,
                        null);

        assertNotNull(last2DaysJournalEntries);
        assertEquals(2, last2DaysJournalEntries.size());
    }

    @Test
    void givenStartDate_whenGettingJournalEntries_thenEntriesWithMatchingStartDateAreReturned(){
        final LocalDateTime now = LocalDateTime.now();
        List<JournalEntry> startDateJournalEntries =
                journalEntryRepository.findWithFilters(null,
                        now.minusDays(2),
                        null,
                        null);

        assertNotNull(startDateJournalEntries);
        assertEquals(2, startDateJournalEntries.size());
    }

    @Test
    void givenEndDate_whenGettingJournalEntries_thenEntriesWithMatchingEndDateAreReturned(){
        final LocalDateTime now = LocalDateTime.now();
        List<JournalEntry> endDateJournalEntries =
                journalEntryRepository.findWithFilters(null,
                        null,
                        now.minusDays(1),
                        null);

        assertNotNull(endDateJournalEntries);
        assertEquals(3, endDateJournalEntries.size());
    }

    @Test
    void givenAccountant_whenGettingJournalEntries_thenEntriesWithMatchingAccountantAreReturned(){
        final LocalDateTime now = LocalDateTime.now();
        final String assignedAccountant = "David Marshall 2";
        List<JournalEntry> givenAccountantJournalEntries =
                journalEntryRepository.findWithFilters(null,
                        null,
                        null,
                        assignedAccountant);

        assertNotNull(givenAccountantJournalEntries);
        assertEquals(2, givenAccountantJournalEntries.size());
        for (final JournalEntry entry : givenAccountantJournalEntries){
            assertNotNull(entry.getAssignedAccountant());
            assertEquals(assignedAccountant, entry.getAssignedAccountant().getName());
        }
    }

    @Test
    void givenNoFilters_whenGettingJournalEntries_thenAllEntriesAreReturned(){
        List<JournalEntry> allJournalEntries =
                journalEntryRepository.findWithFilters(null,
                        null,
                        null,
                        null);

        assertNotNull(allJournalEntries);
        assertEquals(5, allJournalEntries.size());
    }

    @Test
    void givenMultipleFilters_whenGettingJournalEntries_thenMatchingEntriesAreReturned(){
        //2025-05-28T08:30:00
        List<JournalEntry> journalEntries =
                journalEntryRepository.findWithFilters(
                        List.of(Status.DRAFT, Status.REVIEWED),
                        null,
                        LocalDateTime.of(2025, 5,28,10,0),
                        null);

        assertNotNull(journalEntries);
        assertEquals(2, journalEntries.size());
    }


}

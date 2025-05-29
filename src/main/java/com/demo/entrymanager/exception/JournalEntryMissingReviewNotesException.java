package com.demo.entrymanager.exception;

public class JournalEntryMissingReviewNotesException extends RuntimeException {
    public JournalEntryMissingReviewNotesException(String msg) {
        super(msg);
    }
}

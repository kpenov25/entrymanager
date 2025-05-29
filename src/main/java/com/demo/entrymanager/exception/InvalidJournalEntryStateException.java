package com.demo.entrymanager.exception;

public class InvalidJournalEntryStateException extends RuntimeException {
    public InvalidJournalEntryStateException(String message) {
        super(message);
    }
}

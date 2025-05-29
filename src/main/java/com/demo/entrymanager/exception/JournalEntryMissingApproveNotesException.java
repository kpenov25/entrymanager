package com.demo.entrymanager.exception;

public class JournalEntryMissingApproveNotesException extends RuntimeException {
    public JournalEntryMissingApproveNotesException(String msg) {
        super(msg);
    }
}

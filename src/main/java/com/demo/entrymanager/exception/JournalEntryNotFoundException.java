package com.demo.entrymanager.exception;

public class JournalEntryNotFoundException extends RuntimeException {
    public JournalEntryNotFoundException(String msg) {
        super(msg);
    }
}

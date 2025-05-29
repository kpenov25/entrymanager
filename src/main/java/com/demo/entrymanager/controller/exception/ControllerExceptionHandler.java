package com.demo.entrymanager.controller.exception;

import com.demo.entrymanager.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(MissingScenarioException.class)
    public ResponseEntity<String> handleMissingScenarioException(MissingScenarioException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<String> handleInvalidDateRangeException(InvalidDateRangeException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JournalEntryMissingApproveNotesException.class)
    public ResponseEntity<String> handleJournalEntryMissingApproveNotesException(JournalEntryMissingApproveNotesException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JournalEntryMissingReviewNotesException.class)
    public ResponseEntity<String> handleJournalEntryMissingReviewNotesException(JournalEntryMissingReviewNotesException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JournalEntryNotFoundException.class)
    public ResponseEntity<String> handleJournalEntryNotFoundException(JournalEntryNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidJournalEntryStateException.class)
    public ResponseEntity<String> handleInvalidJournalEntryState(InvalidJournalEntryStateException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountantNotFoundException.class)
    public ResponseEntity<String> handleAccountantNotFoundException(AccountantNotFoundException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}

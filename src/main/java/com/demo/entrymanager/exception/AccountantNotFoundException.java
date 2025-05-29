package com.demo.entrymanager.exception;

public class AccountantNotFoundException extends RuntimeException {
    public AccountantNotFoundException(String msg) {
        super(msg);
    }
}

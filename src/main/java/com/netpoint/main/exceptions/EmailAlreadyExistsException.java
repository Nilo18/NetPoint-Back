package com.netpoint.main.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("The provided email: " + email + " is already in use.");
    }
}

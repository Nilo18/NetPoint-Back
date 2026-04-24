package com.netpoint.main.exceptions;

public class InvitationTokenNotFoundException extends RuntimeException {
    public InvitationTokenNotFoundException(String message) {
        super(message);
    }
}

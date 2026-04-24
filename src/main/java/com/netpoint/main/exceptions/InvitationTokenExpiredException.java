package com.netpoint.main.exceptions;

public class InvitationTokenExpiredException extends RuntimeException {
    public InvitationTokenExpiredException(String message) {
        super(message);
    }
}

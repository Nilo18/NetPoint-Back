package com.netpoint.main.exceptions;

public class InvitationTokenAlreadyUsedException extends RuntimeException {
    public InvitationTokenAlreadyUsedException(String message) {
        super(message);
    }
}

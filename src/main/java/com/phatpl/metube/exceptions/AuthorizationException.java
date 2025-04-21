package com.phatpl.metube.exceptions;

public class AuthorizationException extends RuntimeException {
    public AuthorizationException() {
        super("Unauthorization");
    }
}

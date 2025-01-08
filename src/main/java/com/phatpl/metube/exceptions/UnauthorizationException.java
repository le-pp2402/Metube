package com.phatpl.metube.exceptions;

public class UnauthorizationException extends RuntimeException {
    public UnauthorizationException() {
        super("Unauthorization");
    }
}

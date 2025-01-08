package com.phatpl.metube.exceptions;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException() {
        super("inactive account");
    }
}

package com.phatpl.metube._exceptions;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException() {
        super("inactive account");
    }
}

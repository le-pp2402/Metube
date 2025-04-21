package com.phatpl.metube.exceptions;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException() {
        super("Please active your account first");
    }
}

package com.phatpl.metube._exceptions;

public class ExistedException extends RuntimeException {
    public ExistedException(String object) {
        super(object + "existed");
    }
}

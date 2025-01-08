package com.phatpl.metube.exceptions;

public class ExpiredException extends RuntimeException {
    public ExpiredException(String str) {
        super(str + "expired");
    }
}

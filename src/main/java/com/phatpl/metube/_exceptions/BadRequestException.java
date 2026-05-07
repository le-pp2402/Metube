package com.phatpl.metube._exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String str) {
        super(str);
    }
}

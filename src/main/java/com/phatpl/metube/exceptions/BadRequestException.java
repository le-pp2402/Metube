package com.phatpl.metube.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String str) {
        super(str);
    }
}

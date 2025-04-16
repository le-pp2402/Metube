package com.phatpl.metube.exceptions;

public class WrongVerifyCode extends RuntimeException {
    public WrongVerifyCode() {
        super("wrong code");
    }
}

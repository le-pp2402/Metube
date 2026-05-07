package com.phatpl.metube._exceptions;

public class WrongVerifyCode extends RuntimeException {
    public WrongVerifyCode() {
        super("wrong code");
    }
}

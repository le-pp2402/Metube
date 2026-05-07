package com.phatpl.metube._exceptions;

public class WrongUsernameOrPassword extends RuntimeException {
    public WrongUsernameOrPassword() {
        super("wrong username or password");
    }
}

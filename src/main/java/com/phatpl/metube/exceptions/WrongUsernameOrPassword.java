package com.phatpl.metube.exceptions;

public class WrongUsernameOrPassword extends RuntimeException {
    public WrongUsernameOrPassword() {
        super("wrong username or password");
    }
}

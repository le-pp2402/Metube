package com.phatpl.metube.exceptions;

import com.phatpl.metube.models.User;

public class AlreadyExistsException extends Exception {
    public AlreadyExistsException(Class<User> clazz, String identity) {
        super(clazz.getSimpleName() + " existed, id = " + identity);
    }
}

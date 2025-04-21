package com.phatpl.metube.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(Class<?> clazz, Object id) {
        super(clazz.getSimpleName() + " not found, id = " + id);
    }
}

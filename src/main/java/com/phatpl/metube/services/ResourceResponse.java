package com.phatpl.metube.services;

import com.phatpl.metube.dtos.BaseDTO;

public class ResourceResponse extends BaseDTO {
    private String message;
    private Object data;

    public ResourceResponse() {
    }

    public ResourceResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public ResourceResponse(String message) {
        this.message = message;
    }

    public ResourceResponse(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}

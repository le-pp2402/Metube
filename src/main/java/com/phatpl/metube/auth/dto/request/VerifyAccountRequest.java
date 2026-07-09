package com.phatpl.metube.auth.dto.request;

public record VerifyAccountRequest(String email, String token) {
}

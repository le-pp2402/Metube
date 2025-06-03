package com.phatpl.metube.dtos.request.identity;

import com.phatpl.metube.utils.Regex;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Pattern(regexp = Regex.USERNAME, message = "Must start with an alphabetic character. Can contain the following characters: [a-z] [A-Z] [0-9] and _")
    private String username;

    @Pattern(regexp = Regex.PASSWORD, message = "8 characters & contain at least 1 uppercase letter, 1 lowercase letter, and 1 number && can contain special characters")
    private String password;

    @Pattern(regexp = Regex.EMAIL, message = "invalid email")
    private String email;
}

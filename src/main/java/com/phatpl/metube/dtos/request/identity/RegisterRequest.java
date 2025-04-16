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
    @Pattern(regexp = Regex.USERNAME, message = "invalid username")
    private String username;

    @Pattern(regexp = Regex.PASSWORD, message = "invalid password")
    private String password;

    @Pattern(regexp = Regex.EMAIL, message = "invalid email")
    private String email;
}

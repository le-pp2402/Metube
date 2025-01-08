package com.phatpl.metube.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UpdatePasswordRequest {

    @Pattern(regexp = Regex.PASSWORD, message = "invalid password format")
    @JsonProperty("old_password")
    private String oldPassword;

    @Pattern(regexp = Regex.PASSWORD, message = "invalid password format")
    @JsonProperty("new_password")
    private String newPassword;

}

package com.phatpl.metube.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.phatpl.metube.dtos.BaseDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse extends BaseDTO {
    private String username;
    private String email;
    @JsonProperty("is_admin")
    private Boolean isAdmin;
    private Integer elo;
    private String token;
}

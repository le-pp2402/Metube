package com.phatpl.metube.dtos.request.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResourceRequest {
    private String title;
    @JsonProperty("is_private")
    private Boolean isPrivate;
    private String description;
}

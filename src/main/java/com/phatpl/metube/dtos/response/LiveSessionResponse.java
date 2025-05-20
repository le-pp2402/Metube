package com.phatpl.metube.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.phatpl.metube.dtos.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveSessionResponse extends BaseDTO {
    private Integer id;
    private String username;
    private String title;
    @JsonProperty("view_count")
    private Long viewCount;
    private String path;
}

package com.phatpl.metube._dtos.response;

import com.phatpl.metube._dtos.BaseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LiveSessionResponse extends BaseDTO {
    private String username;
    private String title;
    private Long viewCount;
    private String path;
}

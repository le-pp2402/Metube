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
public class ResourceResponse extends BaseDTO  {
    String title;
    String video;
    String thumbnail;
    String dateTime;
}


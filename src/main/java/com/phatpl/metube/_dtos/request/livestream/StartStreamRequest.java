package com.phatpl.metube._dtos.request.livestream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StartStreamRequest {
    public String streamKey;
}

package com.phatpl.metube.dtos.request.stream;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {
    private String username;
    private String message;
}

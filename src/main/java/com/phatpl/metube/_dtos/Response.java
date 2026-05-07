package com.phatpl.metube._dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response {
    @JsonProperty("code")
    Integer code;
    @JsonProperty("data")
    Object data;
    @JsonProperty("message")
    String message;
    @JsonProperty("status")
    String status;


    public static Response ok(Object o){
        return Response.builder()
                .message("OK")
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.getReasonPhrase())
                .data(o)
                .build();
    }

    public static Response created(Object o){
        return Response.builder()
                .message("CREATED")
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED.getReasonPhrase())
                .data(o)
                .build();
    }

    public static Response badRequest(Object o){
        return Response.builder()
                .message("BAD_REQUEST")
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .data(o)
                .build();
    }
    public static Response unauthorized(Object o){
        return Response.builder()
                .message("UNAUTHORIZED")
                .code(HttpStatus.UNAUTHORIZED.value())
                .status(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .data(o)
                .build();
    }
    public static Response forbidden(Object o){
        return Response.builder()
                .message("FORBIDDEN")
                .code(HttpStatus.FORBIDDEN.value())
                .status(HttpStatus.FORBIDDEN.getReasonPhrase())
                .data(o)
                .build();
    }

    public static Response notFound(Object o){
        return Response.builder()
                .message("NOT_FOUND")
                .code(HttpStatus.NOT_FOUND.value())
                .status(HttpStatus.NOT_FOUND.getReasonPhrase())
                .data(o)
                .build();
    }


}

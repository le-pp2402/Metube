package com.phatpl.metube._utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.phatpl.metube._dtos.Response;

public class BuildResponse {

        public static ResponseEntity ok(Object o) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                 .body(Response.ok(o));
        }

        public static ResponseEntity created(Object o){
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Response.created(o));
        }

        public static ResponseEntity badRequest(Object o){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Response.badRequest(o));
        }
        public static ResponseEntity unauthorized(Object o){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Response.unauthorized(o));
        }
        public static ResponseEntity forbidden(Object o){
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Response.forbidden(o));
        }

        public static ResponseEntity notFound(Object o){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Response.notFound(o));
        }
}

package com.phatpl.metube._dtos;

import jakarta.persistence.MappedSuperclass;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public class BaseDTO {
    private Integer id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

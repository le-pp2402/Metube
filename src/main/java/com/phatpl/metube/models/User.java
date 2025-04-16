package com.phatpl.metube.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User extends BaseModel {

    @Column(length = 50, nullable = false)
    String username;

    @Column(length = 200, nullable = false)
    String password;

    @Column(nullable = false, length = 100)
    String email;

    @Column(updatable = false)
    Boolean isAdmin = false;
    Integer elo = 0;
    Boolean activated = false;


    @Column(name = "StreamKey")
    String streamKey = "!@#$%^&*)(*&HHDS123";

    Integer code;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    LiveSession liveSession;

}

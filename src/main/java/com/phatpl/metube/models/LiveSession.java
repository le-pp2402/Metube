package com.phatpl.metube.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="live_session")
public class LiveSession extends BaseModel {
    private String title;
    private Long viewCount;
    private String path;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}

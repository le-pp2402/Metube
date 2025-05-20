package com.phatpl.metube.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="live_session")
public class LiveSession extends BaseModel {
    private String title;
    private Long viewCount;
    private String path;
    private boolean isAccessible;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}

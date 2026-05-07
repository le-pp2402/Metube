package com.phatpl.metube._models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "resources")
public class Resource extends BaseModel {
    @Column(nullable = false)
    String title;
    String video;
    String thumbnail;
    Boolean isPrivate;
    Boolean isReady;

    @ColumnDefault(value = "0")
    Integer viewCount;

    @Column(columnDefinition = "LONGTEXT")
    String summarize;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;


    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "resources_id"
    )
    List<Subtitle> subtitles = new ArrayList<>();
}

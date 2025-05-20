package com.phatpl.metube.models;

import com.phatpl.metube.models.enums.ResourceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

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
    ResourceStatus status;

    @Column(columnDefinition = "LONGTEXT")
    String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ColumnDefault(value = "0")
    Integer viewCount;

    @ColumnDefault(value = "0")
    Integer likeCount;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "resources_id"
    )
    List<Subtitle> subtitles = new ArrayList<>();
}

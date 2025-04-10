package com.phatpl.metube.models;

import com.phatpl.metube.models.enums.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "subtitles")
public class Subtitle extends BaseModel {
    String path;

    @Enumerated(EnumType.ORDINAL)
    Language type;
}

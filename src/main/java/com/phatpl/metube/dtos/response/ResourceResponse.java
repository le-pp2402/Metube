package com.phatpl.metube.dtos.response;

import com.phatpl.metube.dtos.BaseDTO;
import com.phatpl.metube.models.enums.ResourceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponse extends BaseDTO  {
    Integer id;
    String title;
    String video;
    String thumbnail;
    ResourceStatus status;
    Boolean isPrivate;
    String username;
    String dateTime;
    Integer viewCount;
    Integer likeCount;
    String description;
}


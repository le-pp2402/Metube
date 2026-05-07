package com.phatpl.metube._dtos.request.video;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import com.phatpl.metube._utils.Constant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResourceRequest {
    @NotNull(message = "Title " + Constant.NOT_NULL)
    public String title;

    @NotNull(message = "Video " + Constant.NOT_NULL)
    public MultipartFile video;
}

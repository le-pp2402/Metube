package com.phatpl.metube.dtos.request;

import com.phatpl.metube.utils.Constant;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadResourceRequest {
    @NotNull(message = "Title " + Constant.NOT_NULL)
    public String title;

    @NotNull(message = "Video " + Constant.NOT_NULL)
    public MultipartFile video;

    public MultipartFile enSub;

    public MultipartFile viSub;

    public MultipartFile thumbnail;

    @NotNull(message = "isPrivate " + Constant.NOT_NULL)
    public Boolean isPrivate;
}

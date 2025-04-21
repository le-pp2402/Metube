package com.phatpl.metube.dtos.request.video;

import com.phatpl.metube.utils.Constant;
import jakarta.validation.constraints.NotNull;

public class UploadResourceReq {
    @NotNull(message = "Title " + Constant.NOT_NULL)
    public String title;
}

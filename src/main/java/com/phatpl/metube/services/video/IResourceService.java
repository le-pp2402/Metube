package com.phatpl.metube.services.video;

import com.phatpl.metube.dtos.request.video.UploadResourceReq;
import com.phatpl.metube.dtos.response.ResourceResponse;

public interface IResourceService {
    ResourceResponse save(UploadResourceReq req, String path) throws Exception;
}

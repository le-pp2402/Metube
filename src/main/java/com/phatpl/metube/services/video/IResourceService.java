package com.phatpl.metube.services.video;

import com.phatpl.metube.dtos.request.video.UploadResourceRequest;
import com.phatpl.metube.dtos.response.PresignUrlResponse;
import com.phatpl.metube.dtos.response.ResourceResponse;

public interface IResourceService {
    PresignUrlResponse save(UploadResourceRequest req) throws Exception;
}

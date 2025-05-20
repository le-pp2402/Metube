package com.phatpl.metube.controllers.video;

import com.phatpl.metube.dtos.request.GetStorageResourceReq;
import com.phatpl.metube.dtos.request.video.UploadResourceRequest;
import com.phatpl.metube.models.Resource;
import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.video.IResourceService;
import com.phatpl.metube.services.video.ResourceService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/storage")
public class GeneratePreSignedUrl {

    private final ResourceService resourcesService;

    @Autowired
    public GeneratePreSignedUrl(ResourceService resourcesService) {
        this.resourcesService = resourcesService;
    }

    @PostMapping("/download")
    public ResponseEntity<?> generateDownloadPreSignedUrl(@RequestBody GetStorageResourceReq request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        var url = minIOService.genGetPreSignedURL(request.resourcePath, bucketName);
//        return ResponseEntity.ok(url);
        return null;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> generateUploadPresignedUrl(@RequestBody UploadResourceRequest request) throws Exception {
        return ResponseEntity.ok(resourcesService.save(request));
    }
}

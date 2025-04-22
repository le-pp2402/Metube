package com.phatpl.metube.controllers.video;

import com.phatpl.metube.dtos.request.GetStorageResourceReq;
import com.phatpl.metube.dtos.request.video.UploadResourceReq;
import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.video.IResourceService;
import com.phatpl.metube.utils.Constant;
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

    @Value("${BUCKET_NAME}")
    private String bucketName;
    private final MinIOService minIOService;
    private final IResourceService resourcesService;

    @Autowired
    public GeneratePreSignedUrl(MinIOService minIOService, IResourceService resourcesService) {
        this.minIOService = minIOService;
        this.resourcesService = resourcesService;
    }

    @PostMapping("/download")
    public ResponseEntity<?> generateDownloadPreSignedUrl(@RequestBody GetStorageResourceReq request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var url = minIOService.genGetPreSignedURL(request.resourcePath, bucketName);
        return ResponseEntity.ok(url);
    }


    // TODO: ADD TRANSACTION
    @PostMapping("/upload")
    public ResponseEntity<?> generateUploadPresignedUrl(@RequestBody UploadResourceReq request) throws Exception {
        String path = String.valueOf(System.currentTimeMillis()) + "/video/video";
        resourcesService.save(request, path);
        var url = minIOService.genUploadPresignedUrl(path, bucketName);
        return ResponseEntity.ok(url);
    }
}

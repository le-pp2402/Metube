package com.phatpl.metube.controllers.video;

import com.phatpl.metube.dtos.request.GetStorageResourceReq;
import com.phatpl.metube.dtos.request.video.UploadResourceReq;
import com.phatpl.metube.services.MinIOService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/storage")
public class GeneratePreSignedUrl {

    private final MinIOService minIOService;

    @Autowired
    public GeneratePreSignedUrl(MinIOService minIOService) {
        this.minIOService = minIOService;
    }

    @PostMapping("/download")
    public ResponseEntity<?> generateDownloadPreSignedUrl(@RequestBody GetStorageResourceReq request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var url = minIOService.getPreSignedURL(request.resourcePath, request.bucket);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> generateUploadPresignedUrl(@RequestBody UploadResourceReq request) {
        return null;
    }
}

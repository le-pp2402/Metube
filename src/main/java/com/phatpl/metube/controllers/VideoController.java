package com.phatpl.metube.controllers;

import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Value("${WHISPER_API}")
    private String linkWhisperService;
    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    MinIOService minIOService;
    ResourceService resourceService;

    @Autowired
    public VideoController(MinIOService minIOService, ResourceService resourceService) {
        this.minIOService = minIOService;
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<?> loadAll() {
        try {
            return BuildResponse.ok(resourceService.findAllDTO());
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/{folder}/video/{file}")
    public ResponseEntity<?> loadVideo(@PathVariable("folder") String folder,
                                    @PathVariable("file") String file) {
        try {
            var response = resourceService.getVideo(folder, file);
            byte[] resource = response.readAllBytes();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/{folder}/x/{file}")
    public ResponseEntity<?> loadSubtitle(@PathVariable("folder") String folder,
                                       @PathVariable("file") String file) {
        try {
            var response = resourceService.getSubtitle(folder, file);
            byte[] resource = response.readAllBytes();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/{folder}/thumbnail")
    public ResponseEntity loadThumbnail(@PathVariable("folder") String folder)
            throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        var response = minIOService.getImage(folder + "/thumbnail");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(response.readAllBytes());
    }


    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/gen/{id}")
    public ResponseEntity requestGenerateSubFile(@PathVariable("id") Integer id) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(linkWhisperService + id))
                    .header("Content-type", "text")
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return BuildResponse.ok(res.body());
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}

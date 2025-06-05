package com.phatpl.metube.controllers.video;

import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.video.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
public class VideoController {
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
            // :)
            if (file.startsWith("QUANGCAO_")) {
                folder = "1749141885754";
                file = file.substring("QUANGCAO_".length());
            }
            var response = resourceService.getVideo(folder, file);
            byte[] resource = response.readAllBytes();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}

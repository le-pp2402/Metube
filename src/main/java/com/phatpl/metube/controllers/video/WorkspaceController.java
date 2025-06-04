package com.phatpl.metube.controllers.video;

import com.phatpl.metube.dtos.request.video.UpdateResourceRequest;
import com.phatpl.metube.services.video.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final ResourceService resourceService;

    @GetMapping("/content")
    public ResponseEntity<?> getUserContent(@RequestParam(required = false) String searchPattern) {
        try {
            return BuildResponse.ok(resourceService.getUserContent(searchPattern));
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/content/{id}")
    public ResponseEntity<?> getUserContentById(@PathVariable Integer id) {
        try {
            return BuildResponse.ok(resourceService.getUserContentById(id));
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @PutMapping("/content/{id}")
    public ResponseEntity<?> updateUserContentById(@PathVariable Integer id, @RequestBody UpdateResourceRequest request) {
        try {
            if (resourceService.update(request, id)) {
                return BuildResponse.ok(true);
            } else {
                return BuildResponse.badRequest("Video status invalid");
            }
        } catch (EntityNotFoundException e) {
            return BuildResponse.notFound(e.getMessage());
        } catch (RuntimeException | IOException | MinioException |
                 NoSuchAlgorithmException | InvalidKeyException e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}

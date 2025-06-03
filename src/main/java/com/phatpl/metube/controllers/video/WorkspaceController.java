package com.phatpl.metube.controllers.video;

import com.phatpl.metube.services.video.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final ResourceService resourceService;

    @GetMapping("/content")
    public ResponseEntity<?> getUserContent(@RequestParam String searchPattern) {
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
}

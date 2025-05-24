package com.phatpl.metube.controllers.video;

import com.phatpl.metube.dtos.response.ResourceDetailDTO;
import com.phatpl.metube.services.video.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    private final ResourceService resourceService;

    @Autowired
    public WorkspaceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/content") 
    public ResponseEntity<?> getUserContent() {
        try {
            return BuildResponse.ok(resourceService.getUserContent());
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/content/{id}")
    public ResponseEntity<?> getUserContentById(@PathVariable Integer id) {
        try {
            return BuildResponse.ok(resourceService.getUserContentById(id));
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}

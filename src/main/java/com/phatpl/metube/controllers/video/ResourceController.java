package com.phatpl.metube.controllers.video;

import com.phatpl.metube.exceptions.AuthorizationException;
import com.phatpl.metube.services.video.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            resourceService.deleteById(id);
            return BuildResponse.ok("deleted resources id = " + id);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<?> findAll(@RequestParam(required = false) String searchPattern) {
        return BuildResponse.ok(
            resourceService.findAll(searchPattern)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        try {
            return BuildResponse.ok(
                    resourceService.findDTOById(id)
            );
        } catch (AuthorizationException exception) {
            return BuildResponse.unauthorized("unauthorized");
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}

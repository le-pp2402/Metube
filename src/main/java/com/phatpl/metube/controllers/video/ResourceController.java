package com.phatpl.metube.controllers.video;

import com.phatpl.metube.controllers.BaseController;
import com.phatpl.metube.dtos.response.ResourceResponse;
import com.phatpl.metube.filters.BaseFilter;
import com.phatpl.metube.models.Resource;
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
    public ResponseEntity<?> findAll(String searchPattern) {
        return BuildResponse.ok(
            resourceService.findAll(searchPattern)
        );
    }
}

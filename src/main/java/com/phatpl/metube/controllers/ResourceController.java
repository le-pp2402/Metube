package com.phatpl.metube.controllers;

import com.phatpl.metube.dtos.request.SummarizeRequest;
import com.phatpl.metube.dtos.request.UpdateResourceRequest;
import com.phatpl.metube.dtos.request.UploadResourceRequest;
import com.phatpl.metube.dtos.response.ResourceResponse;
import com.phatpl.metube.exceptions.UnauthorizationException;
import com.phatpl.metube.filters.ResourcesFilter;
import com.phatpl.metube.models.Resource;
import com.phatpl.metube.services.GeminiService;
import com.phatpl.metube.services.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.core.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/resources")
public class ResourceController extends BaseController<Resource, ResourceResponse, ResourcesFilter, Integer> {

    private final ResourceService resourceService;
    private final GeminiService geminiService;

    @Autowired
    public ResourceController(ResourceService resourceService, GeminiService geminiService) {
        super(resourceService);
        this.resourceService = resourceService;
        this.geminiService = geminiService;
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@ModelAttribute @Validate UploadResourceRequest request) {
        try {
            return BuildResponse.ok(resourceService.save(request));
        } catch (UnauthorizationException e) {
            return BuildResponse.unauthorized(e.getMessage());
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        try {
            return BuildResponse.ok(resourceService.findDTOById(id));
        } catch (EntityNotFoundException e) {
            return BuildResponse.notFound(e.getMessage());
        } catch (RuntimeException e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        try {
            resourceService.deleteById(id);
            return BuildResponse.ok("deleted resources id = " + id);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @Override
    @PostMapping
    public ResponseEntity<?> findAll(@RequestBody ResourcesFilter request) {
        try {
            return BuildResponse.ok(
                    resourceService.search(request)
            );
        } catch (RuntimeException e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAll() {
        resourceService.deleteAll();
        return BuildResponse.ok("deleted");
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Integer id, @RequestBody UpdateResourceRequest request) {
        try {
            return BuildResponse.ok(resourceService.update(request, id));
        } catch (EntityNotFoundException e) {
            return BuildResponse.notFound(e.getMessage());
        } catch (RuntimeException | IOException | MinioException |
                 NoSuchAlgorithmException | InvalidKeyException e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/gen/{id}")
    public ResponseEntity<?> generateSummarize(@PathVariable("id") Integer id) {
        try {
            String content = resourceService.readSubFile(id);
            String summarize = geminiService.generator(content);
            var hm = new HashMap<String, String>();
            hm.put("summarize", summarize);
            return BuildResponse.ok(hm);
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/gen/{id}")
    public ResponseEntity<?> setSummarize(@PathVariable("id") Integer id, @RequestBody SummarizeRequest request) {
        try {
            return BuildResponse.ok(resourceService.setSummarize(id, request.getSummarize()));
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}

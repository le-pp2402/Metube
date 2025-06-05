package com.phatpl.metube.controllers.common;

import com.phatpl.metube.dtos.request.identity.LoginRequest;
import com.phatpl.metube.exceptions.AuthorizationException;
import com.phatpl.metube.services.video.ResourceService;
import com.phatpl.metube.utils.BuildResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/statistic")
public class StatisticsController {

    private final ResourceService resourceService;

    @Autowired
    public StatisticsController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<?> getStatistic() {
        try {
            return BuildResponse.ok(resourceService.getStatistic());
        } catch (AuthorizationException authorizationException) {
            return BuildResponse.unauthorized("Unauthorized");
        } catch (Exception e) {
            return BuildResponse.badRequest(e.getMessage());
        }
    }
}

package com.phatpl.metube.services.video;

import com.meilisearch.sdk.Index;
import com.phatpl.metube.dtos.request.video.UploadResourceReq;
import com.phatpl.metube.dtos.request.video.UploadResourceRequest;
import com.phatpl.metube.dtos.response.ResourceResponse;
import com.phatpl.metube.exceptions.AuthorizationException;
import com.phatpl.metube.exceptions.BadRequestException;
import com.phatpl.metube.filters.ResourcesFilter;
import com.phatpl.metube.mappers.BaseMapper;
import com.phatpl.metube.mappers.ResourceResponseMapper;
import com.phatpl.metube.models.Resource;
import com.phatpl.metube.repositories.BaseRepository;
import com.phatpl.metube.repositories.ResourceRepository;
import com.phatpl.metube.repositories.UserRepository;
import com.phatpl.metube.services.BaseService;
import com.phatpl.metube.services.MeliSearchService;
import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.UserService;
import com.phatpl.metube.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceService2 extends BaseService<Resource, ResourceResponse, ResourcesFilter, Integer> implements IResourceService {
    ResourceRepository resourceRepository;
    ResourceResponseMapper resourceResponseMapper;
    MinIOService minIOService;
    UserRepository userRepository;
    MeliSearchService meliSearchService;
    UserService userService;
    RabbitMQTranscodingService rabbitMQTranscodingService;

    @Autowired
    public ResourceService2(ResourceRepository resourceRepository, ResourceResponseMapper resourceResponseMapper, MinIOService minIOService, UserRepository userRepository, MeliSearchService meliSearchService, Index index, UserService userService, RabbitMQTranscodingService rabbitMQTranscodingService) {
        super(resourceResponseMapper, resourceRepository);
        this.resourceRepository = resourceRepository;
        this.resourceResponseMapper = resourceResponseMapper;
        this.minIOService = minIOService;
        this.userRepository = userRepository;
        this.meliSearchService = meliSearchService;
        this.userService = userService;
        this.rabbitMQTranscodingService = rabbitMQTranscodingService;
    }


    @Override
    public ResourceResponse save(UploadResourceReq req, String path) throws Exception {
        var userid = userService.extractUserId();
        var user = userRepository.findById(userid).orElseThrow(AuthorizationException::new);

        Resource resource = new Resource();
        resource.setTitle(req.getTitle());
        resource.setIsPrivate(false);
        resource.setUser(user);
        resource.setIsReady(false);
        resource.setVideo(path);
        resource = resourceRepository.save(resource);

        return resourceResponseMapper.toDTO(resource);
    }
}

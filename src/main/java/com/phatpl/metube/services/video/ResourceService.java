package com.phatpl.metube.services.video;

import com.phatpl.metube.dtos.request.video.UpdateResourceRequest;
import com.phatpl.metube.dtos.request.video.UploadResourceRequest;
import com.phatpl.metube.dtos.response.ResourceDetailDTO;
import com.phatpl.metube.dtos.response.ResourceResponse;
import com.phatpl.metube.dtos.response.PresignUrlResponse;
import com.phatpl.metube.exceptions.BadRequestException;
import com.phatpl.metube.exceptions.AuthorizationException;
import com.phatpl.metube.filters.BaseFilter;
import com.phatpl.metube.mappers.ResourceDetailMapper;
import com.phatpl.metube.mappers.ResourceResponseMapper;
import com.phatpl.metube.models.Resource;
import com.phatpl.metube.models.enums.ResourceStatus;
import com.phatpl.metube.repositories.ResourceRepository;
import com.phatpl.metube.repositories.UserRepository;
import com.phatpl.metube.services.BaseService;
import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.UserService;
import com.phatpl.metube.utils.Constant;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Transactional
public class ResourceService extends BaseService<Resource, ResourceResponse, BaseFilter, Integer> implements IResourceService {

    ResourceRepository resourceRepository;
    ResourceResponseMapper resourceResponseMapper;
    MinIOService minIOService;
    UserRepository userRepository;
    UserService userService;
    ResourceDetailMapper resourcesDetailMapper;
    String OldPattern;
    String NewPattern;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository,
                           ResourceResponseMapper resourceResponseMapper,
                           MinIOService minIOService,
                           UserRepository userRepository,
                           UserService userService,
                           ResourceDetailMapper resourcesDetailMapper,
                           @Value("MINIO_URL_OLD_PATTERN") String oldPattern,
                           @Value("MINIO_URL_NEW_PATTERN") String newPattern) {
        super(resourceResponseMapper, resourceRepository);
        this.resourceRepository = resourceRepository;
        this.resourceResponseMapper = resourceResponseMapper;
        this.minIOService = minIOService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.resourcesDetailMapper = resourcesDetailMapper;
        OldPattern = oldPattern;
        NewPattern = newPattern;
    }

    
    public PresignUrlResponse save(UploadResourceRequest req) throws Exception {
        var userid = userService.extractUserId();
        var user = userRepository.findById(userid).orElseThrow(AuthorizationException::new);

        String baseDir = String.valueOf(System.currentTimeMillis());
        String fileName = "Video_" + user.getUsername() + "_" + System.currentTimeMillis();
        String filePath = baseDir + "/video/" + fileName;

        String uploadUrl = minIOService.genPreSignedUrl(
            filePath,
            Constant.BUCKET,
            Method.PUT,
            3,
            TimeUnit.HOURS
        );

        Resource resource = new Resource();
        resource.setTitle(req.getTitle());

        resource.setIsPrivate(true);
        resource.setStatus(ResourceStatus.UPLOADING);

        resource.setUser(user);
        resource.setLikeCount(0);
        resource.setViewCount(0);
        resource.setVideo(filePath);

        resourceRepository.save(resource);
        return new PresignUrlResponse(uploadUrl);
    }



    public void deleteById(Integer id) {
        try {
            var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);

            minIOService.delete(resource.getVideo());
            resourceRepository.deleteById(id);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public boolean update(UpdateResourceRequest request, Integer id) throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        log.info("[WORKSPACE CONTROLLER -> RESOURCES SERVICE]: {}", id.toString());
        var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        if (!Objects.equals(resource.getUser().getId(), id)) {
            throw new AuthorizationException();
        }

        if (resource.getStatus() != ResourceStatus.READY) {
            return false;
        }

        resource.setTitle(request.getTitle());
        resource.setIsPrivate(request.getIsPrivate());
        resource.setDescription(request.getDescription());
        return true;
    }

    public InputStream getVideo(String folder, String file) throws Exception {
        return minIOService.getFile(folder + "/video/" + file);
    }

    public InputStream getSubtitle(String folder, String file) throws Exception {
        return minIOService.getFile(folder + "/subtitle/" + file);
    }

    public Integer getViewCount(Integer id) {
        return resourceRepository.getViewCountById(id);
    }

    public List<ResourceResponse> getUserContent(String searchPattern) {
        var userid = userService.extractUserId();
        var user = userRepository.findById(userid).orElseThrow(AuthorizationException::new);
        if (searchPattern == null) searchPattern = "";
        var resources = resourceRepository.findByUserAndTitleContainsIgnoreCase(user, searchPattern);
        return resourceResponseMapper.toListDTO(resources);
    }


    public ResourceDetailDTO getUserContentById(Integer id) {
        var userid = userService.extractUserId();
        var resources = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        if (!resources.getUser().getId().equals(userid)) {
            throw new AuthorizationException();
        }
        return resourcesDetailMapper.toDTO(resources);
    }

    public List<ResourceResponse> findAll(String searchPattern) {
        var resources = resourceRepository.findByTitleContainsIgnoreCaseAndIsPrivate(searchPattern, false);
        return resourceResponseMapper.toListDTO(resources);
    }
}

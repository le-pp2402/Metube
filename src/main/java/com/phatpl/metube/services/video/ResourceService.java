package com.phatpl.metube.services.video;

import com.meilisearch.sdk.Index;
import com.phatpl.metube.dtos.request.video.UpdateResourceRequest;
import com.phatpl.metube.dtos.request.video.UploadResourceRequest;
import com.phatpl.metube.dtos.response.ResourceResponse;
import com.phatpl.metube.exceptions.BadRequestException;
import com.phatpl.metube.exceptions.UnauthorizationException;
import com.phatpl.metube.filters.ResourcesFilter;
import com.phatpl.metube.mappers.ResourceResponseMapper;
import com.phatpl.metube.models.Resource;
import com.phatpl.metube.repositories.ResourceRepository;
import com.phatpl.metube.repositories.UserRepository;
import com.phatpl.metube.services.BaseService;
import com.phatpl.metube.services.MeliSearchService;
import com.phatpl.metube.services.MinIOService;
import com.phatpl.metube.services.UserService;
import com.phatpl.metube.utils.Constant;
import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Transactional
public class ResourceService extends BaseService<Resource, ResourceResponse, ResourcesFilter, Integer> {

    ResourceRepository resourceRepository;
    ResourceResponseMapper resourceResponseMapper;
    MinIOService minIOService;
    UserRepository userRepository;
    MeliSearchService meliSearchService;
    UserService userService;
    RabbitMQService rabbitMQService;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository, ResourceResponseMapper resourceResponseMapper, MinIOService minIOService, UserRepository userRepository, MeliSearchService meliSearchService, Index index, UserService userService, RabbitMQService rabbitMQService) {
        super(resourceResponseMapper, resourceRepository);
        this.resourceRepository = resourceRepository;
        this.resourceResponseMapper = resourceResponseMapper;
        this.minIOService = minIOService;
        this.userRepository = userRepository;
        this.meliSearchService = meliSearchService;
        this.userService = userService;
        this.rabbitMQService = rabbitMQService;
    }


    // upload video to minio
    public ResourceResponse save(UploadResourceRequest req) throws Exception {
        var userid = userService.extractUserId();
        var user = userRepository.findById(userid).orElseThrow(UnauthorizationException::new);
        String contextType = req.getVideo().getContentType();

        Resource resource = new Resource();
        resource.setTitle(req.getTitle());
        resource.setIsPrivate(false);
        resource.setUser(user);
        resource.setIsReady(false);

        if (contextType != null && contextType.startsWith("video")) {
            var mediaInfo = uploadVideo(req.getVideo());

            resource.setVideo(mediaInfo.get("video"));

            var newElem = resourceRepository.save(resource);
            meliSearchService.addDocument(newElem.getId(), newElem.getTitle(), newElem.getCreatedAt(), newElem.getIsPrivate());

            rabbitMQService.SendMessage(newElem.getId(), Constant.VIDEO_TRANSCODING_QUEUE);

            return resourceResponseMapper.toDTO(newElem);
        } else {
            throw new BadRequestException(Constant.INVALID_FORMAT_FILE);
        }
    }

    // upload video to minio
    private HashMap<String, String> uploadVideo(MultipartFile video) throws Exception {
        var baseDir = String.valueOf(System.currentTimeMillis());
        var mediaInfo = new HashMap<String, String>();
        var path = minIOService.uploadVideo(video.getInputStream(), baseDir + "/video/" + "video", video.getContentType());
        mediaInfo.put("video", path);
        return mediaInfo;
    }

    // https://www.meilisearch.com/docs/reference/api/search#search-parameters
    public List<ResourceResponse> search(ResourcesFilter request) {
        var results = meliSearchService.search(request.getSearchRequest()).getHits();
        var resources = new ArrayList<Resource>();
        results.forEach(e ->
                resources.add(resourceRepository.findById((int) Math.round((double) e.get("id"))).orElse(null)));
        return resourceResponseMapper.toListDTO(resources);
    }

    public void deleteById(Integer id) {
        try {
            var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);

            minIOService.delete(resource.getVideo());

            meliSearchService.deleteById(id);

            resourceRepository.deleteById(id);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

    }

    public void deleteAll() {
        meliSearchService.deleteAll();
        resourceRepository.deleteAll();
    }

    public ResourceResponse update(UpdateResourceRequest request, Integer id) throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        resource.setTitle(request.getTitle());
        resource.setIsPrivate(request.getIsPrivate());
        meliSearchService.update(id, resource.getTitle(), resource.getIsPrivate());
        return resourceResponseMapper.toDTO(resource);
    }

    public InputStream getVideo(String folder, String file) throws Exception {
        return minIOService.getFile(folder + "/video/" + file);
    }

    public InputStream getSubtitle(String folder, String file) throws Exception {
        return minIOService.getFile(folder + "/subtitle/" + file);
    }

//    public String readSubFile(Integer id) throws Exception {
//        var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
//        var input = minIOService.getFile(resource.getEnSub());
//
//        var summarize = new StringBuilder();
//        try (Reader reader = new BufferedReader(new InputStreamReader
//                (input, StandardCharsets.UTF_8))) {
//            int c = 0;
//            while ((c = reader.read()) != -1) {
//                if (Character.isLetter(c) || Character.isSpaceChar(c) || Character.isDigit(c))
//                    summarize.append((char) c);
//                else
//                    summarize.append(' ');
//            }
//        }
//        return summarize.toString();
//    }

    public ResourceResponse setSummarize(Integer id, String summarize) {
        var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        resource.setSummarize(summarize);
        return resourceResponseMapper.toDTO(resource);
    }


    public ResourceResponse save(String streamKey, String fileName) {
        return null;
    }

    public Integer getViewCount(Integer id) {
        return resourceRepository.getViewCountById(id);
    }
}
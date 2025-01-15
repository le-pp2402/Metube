package com.phatpl.metube.services;

import com.meilisearch.sdk.Index;
import com.phatpl.metube.dtos.request.UpdateResourceRequest;
import com.phatpl.metube.dtos.request.UploadResourceRequest;
import com.phatpl.metube.exceptions.BadRequestException;
import com.phatpl.metube.exceptions.UnauthorizationException;
import com.phatpl.metube.filters.ResourcesFilter;
import com.phatpl.metube.mappers.ResourceResponseMapper;
import com.phatpl.metube.models.Resource;
import com.phatpl.metube.repositories.ResourceRepository;
import com.phatpl.metube.repositories.UserRepository;
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
import java.nio.charset.StandardCharsets;
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

    @Autowired
    public ResourceService(ResourceRepository resourceRepository, ResourceResponseMapper resourceResponseMapper, MinIOService minIOService, UserRepository userRepository, MeliSearchService meliSearchService, Index index, UserService userService) {
        super(resourceResponseMapper, resourceRepository);
        this.resourceRepository = resourceRepository;
        this.resourceResponseMapper = resourceResponseMapper;
        this.minIOService = minIOService;
        this.userRepository = userRepository;
        this.meliSearchService = meliSearchService;
        this.userService = userService;
    }

    public ResourceResponse save(UploadResourceRequest req) throws Exception {
        var userid = userService.extractUserId();
        var user = userRepository.findById(userid).orElseThrow(UnauthorizationException::new);
        String contextType = req.getVideo().getContentType();

        Resource resource = new Resource();
        resource.setTitle(req.getTitle());
        resource.setIsPrivate(req.getIsPrivate());
        resource.setUser(user);
        resource.setIsReady(false);

        if (contextType != null && contextType.startsWith("video")) {
            var mediaInfo = uploadVideo(req.getVideo(), req.getEnSub(), req.getViSub(), req.getThumbnail());

            resource.setVideo(mediaInfo.get("video"));
            resource.setViSub(mediaInfo.get("visub"));
            resource.setEnSub(mediaInfo.get("ensub"));
            resource.setThumbnail(mediaInfo.get("thumbnail"));

            var newElem = resourceRepository.save(resource);
            meliSearchService.addDocument(newElem.getId(), newElem.getTitle(), newElem.getCreatedAt(), newElem.getIsPrivate());

            return resourceResponseMapper.toDTO(newElem);
        } else {
            throw new BadRequestException(Constant.INVALID_FORMAT_FILE);
        }
    }

    private HashMap<String, String> uploadVideo(MultipartFile video, MultipartFile enSub, MultipartFile viSub, MultipartFile thumbnail) throws Exception {
        var baseDir = String.valueOf(System.currentTimeMillis());
        var mediaInfo = new HashMap<String, String>();

        var enSubPath = minIOService.uploadDocument(enSub, baseDir + Constant.SUBTITLE_EN);
        var viSubPath = minIOService.uploadDocument(viSub, baseDir + Constant.SUBTITLE_VI);

        var path = minIOService.uploadVideo(video.getInputStream(), baseDir + "/video/" + "video", video.getContentType());
        mediaInfo.put("video", path);
        if (thumbnail != null) {
            var thumbnailPath = minIOService.uploadDocument(thumbnail, baseDir + "/thumbnail");
            mediaInfo.put("thumbnail", thumbnailPath);
        }

        mediaInfo.put("ensub", enSubPath);
        mediaInfo.put("visub", viSubPath);

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
            minIOService.delete(resource.getEnSub());
            minIOService.delete(resource.getViSub());

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

    public String readSubFile(Integer id) throws Exception {
        var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        var input = minIOService.getFile(resource.getEnSub());

        var summarize = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (input, StandardCharsets.UTF_8))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                if (Character.isLetter(c) || Character.isSpaceChar(c) || Character.isDigit(c))
                    summarize.append((char) c);
                else
                    summarize.append(' ');
            }
        }
        return summarize.toString();
    }

    public ResourceResponse setSummarize(Integer id, String summarize) {
        var resource = resourceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        resource.setSummarize(summarize);
        return resourceResponseMapper.toDTO(resource);
    }


    public ResourceResponse save(String streamKey, String fileName) {
        return null;
    }
}
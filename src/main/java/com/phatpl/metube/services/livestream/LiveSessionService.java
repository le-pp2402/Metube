package com.phatpl.metube.services.livestream;

import com.phatpl.metube.dtos.request.livestream.InititalizeStreamingRequest;
import com.phatpl.metube.dtos.response.LiveSessionResponse;
import com.phatpl.metube.exceptions.AuthorizationException;
import com.phatpl.metube.filters.BaseFilter;
import com.phatpl.metube.mappers.LiveSessionMapper;
import com.phatpl.metube.models.LiveSession;
import com.phatpl.metube.repositories.LiveSessionRepository;
import com.phatpl.metube.services.BaseService;
import com.phatpl.metube.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiveSessionService extends BaseService<LiveSession, LiveSessionResponse, BaseFilter, Integer> {

    private final LiveSessionRepository liveSessionRepository;
    private final LiveSessionMapper liveSessionMapper;
    private final UserService userService;

    @Autowired
    public LiveSessionService(LiveSessionRepository liveSessionRepository, LiveSessionMapper liveSessionMapper, UserService userService) {
        super(liveSessionMapper, liveSessionRepository);
        this.liveSessionRepository = liveSessionRepository;
        this.liveSessionMapper = liveSessionMapper;
        this.userService = userService;
    }

    public LiveSessionResponse createLiveSession(InititalizeStreamingRequest request) {
        var userId = userService.extractUserId();
        if (userId == null) {
            throw new AuthorizationException();
        }

        var user = userService.findById(userId);

        var liveSession = liveSessionRepository.findByUserId(user.getId()).orElse(
                LiveSession.builder()
                        .path(user.getUsername() + ".m3u8")
                        .viewCount(0L)
                        .user(user)
                        .build()
        );

        liveSession.setTitle(request.title);
        liveSession.setAccessible(true);

        liveSession = liveSessionRepository.save(liveSession);
        return liveSessionMapper.toDTO(liveSession);
    }

    public void stopLiveSession() {
        var userId = userService.extractUserId();

        if (userId == null) {
            throw new AuthorizationException();
        }

        var liveSsOtp = liveSessionRepository.findByUserId(userId);

        if (liveSsOtp.isPresent()) {
            var liveSs = liveSsOtp.get();
            liveSs.setAccessible(false);
            liveSessionRepository.save(liveSs);
        }
    }

    public void deleteByUserId(Integer userId) {
        liveSessionRepository.deleteAllByUserId(userId);
    }

    public List<LiveSessionResponse> getAllAccessibleLiveSession() {
        var result = liveSessionRepository.findLiveSessionByIsAccessible(true);
        return liveSessionMapper.toListDTO(result);
    }
}

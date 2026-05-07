package com.phatpl.metube._services.livestream;

import com.phatpl.metube._dtos.request.livestream.InititalizeStreamingRequest;
import com.phatpl.metube._dtos.response.LiveSessionResponse;
import com.phatpl.metube._exceptions.UnauthorizationException;
import com.phatpl.metube._filters.BaseFilter;
import com.phatpl.metube._mappers.LiveSessionMapper;
import com.phatpl.metube._models.LiveSession;
import com.phatpl.metube._repositories.LiveSessionRepository;
import com.phatpl.metube._services.BaseService;
import com.phatpl.metube._services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            throw new UnauthorizationException();
        }

        var user = userService.findById(userId);

        var liveSession = user.getLiveSession();

        if (liveSession == null) {
            liveSession = new LiveSession();
            liveSession.setUser(user);
            liveSession.setViewCount(0L);
            liveSession.setPath(user.getUsername() + ".m3u8");
        }

        liveSession.setTitle(request.title);
        liveSession = liveSessionRepository.save(liveSession);

        return liveSessionMapper.toDTO(liveSession);
    }

    public void deleteByUserId(Integer userId) {
        liveSessionRepository.deleteAllByUserId(userId);
    }
}

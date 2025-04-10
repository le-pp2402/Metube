package com.phatpl.metube.services.livestream;

import com.phatpl.metube.dtos.request.stream.InititalizeStreamingRequest;
import com.phatpl.metube.dtos.response.LiveSessionResponse;
import com.phatpl.metube.exceptions.UnauthorizationException;
import com.phatpl.metube.filters.BaseFilter;
import com.phatpl.metube.mappers.BaseMapper;
import com.phatpl.metube.mappers.LiveSessionMapper;
import com.phatpl.metube.models.LiveSession;
import com.phatpl.metube.repositories.BaseRepository;
import com.phatpl.metube.repositories.LiveSessionRepository;
import com.phatpl.metube.services.BaseService;
import com.phatpl.metube.services.UserService;
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

package com.phatpl.metube.repositories;

import com.phatpl.metube.filters.BaseFilter;
import com.phatpl.metube.models.LiveSession;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveSessionRepository extends BaseRepository<LiveSession, BaseFilter, Integer> {
    void deleteAllByUserId(Integer userId);
    Optional<LiveSession> findByUserId(Integer id);
    List<LiveSession> findLiveSessionByIsAccessible(boolean accessible);
}

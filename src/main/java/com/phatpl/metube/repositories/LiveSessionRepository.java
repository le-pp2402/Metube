package com.phatpl.metube.repositories;

import com.phatpl.metube.filters.BaseFilter;
import com.phatpl.metube.models.LiveSession;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveSessionRepository extends BaseRepository<LiveSession, BaseFilter, Integer> {
    void deleteAllByUserId(Integer userId);
}

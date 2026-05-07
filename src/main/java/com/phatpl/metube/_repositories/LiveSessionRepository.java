package com.phatpl.metube._repositories;

import com.phatpl.metube._filters.BaseFilter;
import com.phatpl.metube._models.LiveSession;

import org.springframework.stereotype.Repository;

@Repository
public interface LiveSessionRepository extends BaseRepository<LiveSession, BaseFilter, Integer> {
    void deleteAllByUserId(Integer userId);
}

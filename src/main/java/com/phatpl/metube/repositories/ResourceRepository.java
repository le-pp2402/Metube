package com.phatpl.metube.repositories;

import com.phatpl.metube.filters.ResourcesFilter;
import com.phatpl.metube.models.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends BaseRepository<Resource, ResourcesFilter, Integer> {
    @NotNull
    Optional<Resource> findById(@NotNull Integer id);

    @NotNull
    List<Resource> findAll();
}

package com.phatpl.metube.repositories;

import com.phatpl.metube.filters.BaseFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, FT extends BaseFilter, ID extends Integer> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    @NotNull
    Optional<T> findById(@NotNull Integer id);
}

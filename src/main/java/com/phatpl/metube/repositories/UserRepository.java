package com.phatpl.metube.repositories;

import com.phatpl.metube.filters.UserFilter;
import com.phatpl.metube.models.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends BaseRepository<User, UserFilter, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @NotNull
    Optional<User> findById(@NotNull Integer id);

    Optional<User> findByStreamKey(String streamKey);
}

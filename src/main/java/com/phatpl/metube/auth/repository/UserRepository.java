package com.phatpl.metube.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.phatpl.metube.auth.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  @Override
  <S extends User> S save(S entity);

  @Override
  Optional<User> findById(Long id);

  Optional<User> findByUsername(String username);
}

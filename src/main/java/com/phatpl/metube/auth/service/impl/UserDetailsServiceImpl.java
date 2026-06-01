package com.phatpl.metube.auth.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.phatpl.metube.auth.model.UserPrincipal;
import com.phatpl.metube.auth.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    var user = userRepository.findByUsername(username)
        .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    return new UserPrincipal(user);
  }

  public UserPrincipal loadUserById(Long userId) {
    var user = userRepository.findById(userId)
        .orElseThrow(() -> UsernameNotFoundException.fromUsername(userId.toString()));

    return new UserPrincipal(user);
  }
}

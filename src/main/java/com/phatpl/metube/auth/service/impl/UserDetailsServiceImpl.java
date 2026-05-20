package com.phatpl.metube.auth.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.phatpl.metube.auth.model.UserPrincipal;
import com.phatpl.metube.auth.repository.UserRepository;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

  private UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      throw UsernameNotFoundException.fromUsername(username);
    }
    return new UserPrincipal(user.get());
  }
}

package com.phatpl.metube.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.phatpl.metube.auth.dto.request.RegisterRequest;
import com.phatpl.metube.auth.model.User;
import com.phatpl.metube.auth.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        var hashedPassword = passwordEncoder.encode(req.password());
        User user = User.register(req.username(), hashedPassword, req.email());
        user = userRepository.save(user);

        // send verify email
    }
}

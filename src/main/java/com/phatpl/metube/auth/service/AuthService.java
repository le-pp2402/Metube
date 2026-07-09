package com.phatpl.metube.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phatpl.metube.auth.dto.request.RegisterRequest;
import com.phatpl.metube.auth.dto.request.VerifyAccountRequest;
import com.phatpl.metube.auth.model.User;
import com.phatpl.metube.auth.model.UserPrincipal;
import com.phatpl.metube.auth.repository.UserRepository;
import com.phatpl.metube.common.outbox.EventType;
import com.phatpl.metube.common.outbox.OutboxEvent;
import com.phatpl.metube.common.outbox.OutboxEventRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    public AuthService(
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper,
            OutboxEventRepository outboxEventRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public void register(RegisterRequest req) throws JsonProcessingException {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        var hashedPassword = passwordEncoder.encode(req.password());
        User user = User.register(req.username(), hashedPassword, req.email());
        user = userRepository.save(user);

        // send verify email
        var verifyToken = jwtService.genAccessToken(new UserPrincipal(user));
        var payload = new VerifyAccountRequest(user.getEmail(), verifyToken);
        OutboxEvent event = new OutboxEvent(EventType.ACCOUNT_VERIFICATION, objectMapper.writeValueAsString(payload));
        outboxEventRepository.save(event);
    }
}

package com.phatpl.metube.services.identity;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.phatpl.metube.dtos.response.LoginResponse;
import com.phatpl.metube.dtos.response.UserResponse;
import com.phatpl.metube.mappers.LoginResponseMapper;
import com.phatpl.metube.mappers.UserResponseMapper;
import com.phatpl.metube.models.User;
import com.phatpl.metube.repositories.UserRepository;
import com.phatpl.metube.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final LoginResponseMapper loginResponseMapper;
    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final JWTService jwtService;

    public LoginResponse verify(String ggToken) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(ggToken);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();

            var user = userRepository.findByEmail(email);

            int id = user.isPresent() ? user.get().getId() : 0;

            if (user.isEmpty()) {
                var newUser = new User();

                newUser.setEmail(email);
                newUser.setUsername("user_" + RandomStringUtils.randomAlphabetic(12));
                newUser.setActivated(true);
                newUser.setPassword(RandomStringUtils.randomAlphabetic(20));

                id = userService.persistEntity(newUser).getId();
            }

            user = userRepository.findById(id);

            if (user.isPresent()) {
                var userDto = userResponseMapper.toDTO(user.get());
                var loginResponse = loginResponseMapper.toDTO(user.get());
                loginResponse.setToken(jwtService.createToken(userDto));
                return loginResponse;
            } else {
                throw new EntityNotFoundException("User with email " + email + " not found");
            }
        } else {
            throw new GeneralSecurityException("Invalid token");
        }
    }
}

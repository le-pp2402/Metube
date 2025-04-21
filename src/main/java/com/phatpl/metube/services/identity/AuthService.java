package com.phatpl.metube.services.identity;

import com.phatpl.metube.dtos.request.identity.LoginRequest;
import com.phatpl.metube.dtos.response.LoginResponse;
import com.phatpl.metube.dtos.response.UserResponse;
import com.phatpl.metube.exceptions.InactiveAccountException;
import com.phatpl.metube.filters.UserFilter;
import com.phatpl.metube.mappers.LoginResponseMapper;
import com.phatpl.metube.mappers.UserResponseMapper;
import com.phatpl.metube.models.User;
import com.phatpl.metube.repositories.UserRepository;
import com.phatpl.metube.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService extends BaseService<User, UserResponse, UserFilter, Integer> {
    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final LoginResponseMapper loginResponseMapper;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserResponseMapper userResponseMapper, UserRepository userRepository, LoginResponseMapper loginResponseMapper, JWTService jwtService, AuthenticationManager authenticationManager) {
        super(userResponseMapper, userRepository);
        this.userRepository = userRepository;
        this.userResponseMapper = userResponseMapper;
        this.loginResponseMapper = loginResponseMapper;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(InactiveAccountException::new);
        if (!user.getActivated())
            throw new InactiveAccountException();

        String token = jwtService.createToken(userResponseMapper.toDTO(user));
        var response = loginResponseMapper.toDTO(user);
        response.setToken(token);

        return response;
    }
}

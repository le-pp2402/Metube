package com.phatpl.metube.services;

import com.phatpl.metube.dtos.request.identity.RegisterRequest;
import com.phatpl.metube.dtos.response.UserResponse;
import com.phatpl.metube.exceptions.*;
import com.phatpl.metube.filters.UserFilter;
import com.phatpl.metube.mappers.RegisterRequestMapper;
import com.phatpl.metube.mappers.UserResponseMapper;
import com.phatpl.metube.models.BaseModel;
import com.phatpl.metube.models.User;
import com.phatpl.metube.repositories.UserRepository;
import com.phatpl.metube.services.identity.MailService;
import com.phatpl.metube.utils.BCryptPassword;
import com.phatpl.metube.utils.MailUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService extends BaseService<User, UserResponse, UserFilter, Integer> {
    UserRepository userRepository;
    MailService mailService;
    UserResponseMapper userResponseMapper;
    RegisterRequestMapper registerRequestMapper;

    @Autowired
    public UserService(UserResponseMapper userResponseMapper, UserRepository userRepository, MailService mailService, RegisterRequestMapper registerRequestMapper) {
        super(userResponseMapper, userRepository);
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.userResponseMapper = userResponseMapper;
        this.registerRequestMapper = registerRequestMapper;
    }

    public UserResponse findByStreamKey(String streamKey) {
        var user = userRepository.findByStreamKey(streamKey);
        return user.map(userResponseMapper::toDTO).orElse(null);
    }

    public UserResponse register(RegisterRequest request) throws AlreadyExistsException {
        String username = request.getUsername();
        String email = request.getEmail();

        if (userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(username).isPresent()) {
            throw new AlreadyExistsException(User.class, email + " or " + username);
        }

        User user = registerRequestMapper.toEntity(request);
        user.setPassword(BCryptPassword.encode(user.getPassword()));
        // TODO: remove code after 5 minutes
        user.setCode(MailUtil.genCode());
        user.setActivated(false);

        userRepository.save(user);
        mailService.sendEmail(MailUtil.genMail(user.getEmail(), user.getCode()));

        return UserResponseMapper.instance.toDTO(user);
    }

    public UserResponse me() {
        var userId = extractUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("user not found"));
        return userResponseMapper.toDTO(user);
    }

    public UserResponse activeUser(String userMail, String code) {
        var optUser = userRepository.findByEmail(userMail);
        if (optUser.isPresent() && optUser.get().getCode().toString().equals(code)) {
            var user = optUser.get();
            user.setActivated(true);
            return userResponseMapper.toDTO(persistEntity(user));
        } else {
            throw new WrongVerifyCode();
        }
    }

    public UserResponse updateUserInfo(String oldPassword, String newPassword) {
        var user = findById(extractUserId());
        if (BCryptPassword.matches(oldPassword, user.getPassword())) {
            user.setPassword(BCryptPassword.encode(newPassword));
            persistEntity(user);
            return userResponseMapper.toDTO(user);
        } else {
            throw new WrongUsernameOrPassword();
        }
    }

    public Integer extractUserId() {
        JwtAuthenticationToken JwtAuthToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String username = JwtAuthToken.getName();
        var user = userRepository.findByUsername(username);
        return user.map(BaseModel::getId).orElse(null);
    }

    public String getStreamKey() {
        var userId = extractUserId();
        var user = findById(userId);
        String streamKey = UUID.randomUUID().toString();
        user.setStreamKey(streamKey);
        userRepository.save(user);
        return streamKey;
    }

    public boolean verifyStreamKey(String streamKey) {
        var user = userRepository.findByStreamKey(streamKey);
        return user.isPresent();
    }
}

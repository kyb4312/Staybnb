package com.staybnb.users.service;

import com.staybnb.common.exception.custom.SignupException;
import com.staybnb.common.auth.jwt.JwtUtils;
import com.staybnb.common.auth.jwt.LogoutTokenService;
import com.staybnb.users.domain.User;
import com.staybnb.common.exception.custom.NoSuchUserException;
import com.staybnb.users.dto.response.UserResponse;
import com.staybnb.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final LogoutTokenService logoutTokenService;

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchUserException(id));
    }

    public UserResponse login(String email, String password) {
//        log.info("step: service entry → {}", Thread.currentThread().getName());
        User user = userRepository.findByEmail(email)
                .orElseThrow(NoSuchUserException::new);

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new NoSuchUserException();
        }

        UserResponse userResponse = UserResponse.fromEntity(user);
        userResponse.setToken(jwtUtils.generateToken(user.getId().toString(), user.getName()));

        return userResponse;
    }

    public void logout(String token) {
        logoutTokenService.logout(token);
    }

    public User signup(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new SignupException(user.getEmail());
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        return userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(long userId) {
        User user = findById(userId);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
    }
}

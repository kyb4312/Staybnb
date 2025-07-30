package com.staybnb.users.service;

import com.staybnb.common.exception.custom.SignupException;
import com.staybnb.common.auth.jwt.JwtUtils;
import com.staybnb.common.auth.jwt.LogoutTokenService;
import com.staybnb.users.domain.User;
import com.staybnb.common.exception.custom.NoSuchUserException;
import com.staybnb.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final LogoutTokenService logoutTokenService;

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchUserException(id));
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> !u.isDeleted())
                .orElseThrow(NoSuchUserException::new);

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new NoSuchUserException();
        }

        return jwtUtils.generateToken(user.getId().toString(), user.getName());
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

package com.staybnb.users.service;

import com.staybnb.common.jwt.JwtUtils;
import com.staybnb.users.domain.User;
import com.staybnb.common.exception.custom.NoSuchUserException;
import com.staybnb.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchUserException(id));
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(NoSuchUserException::new);

        return jwtUtils.generateToken(user.getId().toString(), user.getName());
    }

}

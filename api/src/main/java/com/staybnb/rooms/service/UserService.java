package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.User;
import com.staybnb.rooms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}

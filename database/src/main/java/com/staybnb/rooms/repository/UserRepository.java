package com.staybnb.rooms.repository;

import com.staybnb.rooms.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

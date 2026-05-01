package com.brickwork.users.service;

import com.brickwork.users.dto.UserDTO;
import com.brickwork.users.dto.UserRegistrationDTO;
import com.brickwork.users.entity.User;

import java.util.Optional;

public interface UserService {
    UserDTO registerUser(UserRegistrationDTO registrationDTO);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

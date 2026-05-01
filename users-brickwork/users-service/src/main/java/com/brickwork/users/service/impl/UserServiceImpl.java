package com.brickwork.users.service.impl;

import com.brickwork.users.dto.UserDTO;
import com.brickwork.users.dto.UserRegistrationDTO;
import com.brickwork.users.entity.User;
import com.brickwork.users.repository.UserRepository;
import com.brickwork.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(registrationDTO.getPassword()));
        user.setEmail(registrationDTO.getEmail());
        user.setRole(registrationDTO.getRole());
        user.setFullName(registrationDTO.getFullName());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());
        user.setCompanyName(registrationDTO.getCompanyName());

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::mapToDTO);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Helper method to map Entity to DTO
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCompanyName(user.getCompanyName());
        dto.setCreatedAt(user.getCreatedAt());
        // Notice we explicitly DO NOT map the password back into the DTO!
        return dto;
    }
}

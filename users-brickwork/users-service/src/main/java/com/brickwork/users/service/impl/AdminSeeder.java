package com.brickwork.users.service.impl;

import com.brickwork.users.entity.User;
import com.brickwork.users.enums.Role;
import com.brickwork.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if an admin already exists in the database
        // (Assuming your UserRepository has a method like findByRole or you can check by username)
        Optional<User> existingAdmin = userRepository.findByUsername("superadmin");

        if (existingAdmin.isEmpty()) {
            System.out.println("No Admin found. Bootstrapping default superadmin account...");

            User admin = new User();
            admin.setUsername("superadmin");
            admin.setEmail("admin@brickworks.com");
            admin.setFullName("Super Admin");
            admin.setPhoneNumber("+91-9770016640");
            admin.setCreatedAt(LocalDateTime.now());
            // Automatically hashes the password using BCrypt
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            // Set other required fields if your User entity has them (email, phone, etc.)

            userRepository.save(admin);
            System.out.println("Default Admin created! Username: superadmin | Password: Admin@123");
        }
    }
}
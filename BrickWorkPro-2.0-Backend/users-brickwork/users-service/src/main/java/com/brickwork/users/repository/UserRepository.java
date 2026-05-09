package com.brickwork.users.repository;

import com.brickwork.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Core method for Spring Security login
    Optional<User> findByUsername(String username);

    // Useful for registration validation (preventing duplicate accounts)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
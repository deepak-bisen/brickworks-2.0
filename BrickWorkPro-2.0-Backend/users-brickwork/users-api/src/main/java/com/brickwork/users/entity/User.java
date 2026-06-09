package com.brickwork.users.entity;

import com.brickwork.users.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Authentication Info
    @Column(name = "USERNAME",columnDefinition = "VARCHAR(15)", unique = true, nullable = false)
    private String username;

    @Column(name = "EMAIL", columnDefinition = "VARCHAR(25)", unique = true, nullable = false)
    private String email;

    @Column(name = "PASSWORD", columnDefinition = "VARCHAR(200)", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Common Profile Info
    @Column(name = "FULL_NAME", columnDefinition = "VARCHAR(25)", nullable = false)
    private String fullName;

    @Column(name = "PHONE_NUMBER", columnDefinition = "VARCHAR(15)", nullable = false)
    private String phoneNumber;

    @Column(name = "CREATED_AT", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
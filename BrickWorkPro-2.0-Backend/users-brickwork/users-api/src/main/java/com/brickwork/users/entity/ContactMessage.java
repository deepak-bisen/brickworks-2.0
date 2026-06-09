package com.brickwork.users.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String messageId;

    @Column(name = "NAME", columnDefinition = "VARCHAR(25)", nullable = false)
    private String name;

    @Column(name = "EMAIL", columnDefinition = "VARCHAR(25)", nullable = false)
    private String email;

    // NAYA FIELD: Mobile Number ke liye
    @Column(name = "mobile_number", columnDefinition = "VARCHAR(13)", nullable = false)
    private String mobileNumber;

    @Column(nullable = false, length = 2000)
    private String message;

    // Status ab 3 tarah ke honge: "UNREAD", "READ", "RESOLVED"
    private String status = "UNREAD";

    private LocalDateTime createdAt = LocalDateTime.now();
}
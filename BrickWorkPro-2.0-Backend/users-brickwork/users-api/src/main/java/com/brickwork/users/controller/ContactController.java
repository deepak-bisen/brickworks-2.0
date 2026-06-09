package com.brickwork.users.controller;

import com.brickwork.users.entity.ContactMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/users/contact")
public interface ContactController {

    @PostMapping
    ResponseEntity<ContactMessage> submitMessage(@RequestBody ContactMessage message);

    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAllMessages();

    @PatchMapping("/{messageId}")
    public ResponseEntity<?> markAsRead(@PathVariable String messageId, @RequestBody Map<String, String> updates);
}

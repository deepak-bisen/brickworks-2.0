package com.brickwork.users.controller.impl;

import com.brickwork.users.controller.ContactController;
import com.brickwork.users.entity.ContactMessage;
import com.brickwork.users.service.contact.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ContactControllerImpl implements ContactController {

    private final ContactService contactService;

    @Override
    public ResponseEntity<ContactMessage> submitMessage(ContactMessage message) {
        return ResponseEntity.ok(contactService.submitMessage(message));
    }

    @Override
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        return ResponseEntity.ok(contactService.getAllMessages());
    }

    @Override
    public ResponseEntity<?> markAsRead(String messageId, Map<String, String> updates) {
        return ResponseEntity.ok(contactService.markAsRead(messageId, updates));
    }
}
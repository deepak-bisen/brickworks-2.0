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

    // 1. PUBLIC: For customers submitting forms on the home page
    @Override
    public ResponseEntity<ContactMessage> submitMessage(ContactMessage message) {
        try{
            return ResponseEntity.ok(contactService.submitMessage(message));
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    // 2. ADMIN: For fetching all messages in the dashboard
    @Override
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        try{
            return ResponseEntity.ok(contactService.getAllMessages());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    // 3. ADMIN: For marking a message as read
    @Override
    public ResponseEntity<?> markAsRead(String messageId, Map<String, String> updates) {
        try{
            return ResponseEntity.ok(contactService.markAsRead(messageId, updates));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
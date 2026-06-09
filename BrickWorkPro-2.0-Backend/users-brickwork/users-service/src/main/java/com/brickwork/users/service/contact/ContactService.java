package com.brickwork.users.service.contact;

import com.brickwork.users.entity.ContactMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ContactService {

    ContactMessage submitMessage(ContactMessage message);
    List<ContactMessage> getAllMessages();
    Optional<ContactMessage> markAsRead(String messageId, Map<String, String> updates);
}

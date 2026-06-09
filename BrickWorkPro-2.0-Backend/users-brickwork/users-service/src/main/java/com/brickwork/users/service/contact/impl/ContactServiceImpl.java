package com.brickwork.users.service.contact.impl;

import com.brickwork.users.entity.ContactMessage;
import com.brickwork.users.repository.ContactMessageRepository;
import com.brickwork.users.service.contact.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Override
    public ContactMessage submitMessage(ContactMessage message) {
        return contactMessageRepository.save(message);
    }

    @Override
    public List<ContactMessage> getAllMessages() {
        // Fetch newest messages first
        return contactMessageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional
    public Optional<ContactMessage> markAsRead(String messageId, Map<String, String> updates) {
        Optional<ContactMessage> optionalMessage = contactMessageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            ContactMessage message = optionalMessage.get();
            if (updates.containsKey("status")) {
                message.setStatus(updates.get("status"));
                contactMessageRepository.save(message);
            }
            return Optional.of(message);
        }
        return Optional.empty(); // Explicitly return an empty Optional
    }
}

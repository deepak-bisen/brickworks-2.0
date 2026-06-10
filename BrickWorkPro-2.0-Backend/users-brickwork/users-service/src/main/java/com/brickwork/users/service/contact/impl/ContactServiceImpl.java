package com.brickwork.users.service.contact.impl;

import com.brickwork.users.entity.ContactMessage;
import com.brickwork.users.repository.ContactMessageRepository;
import com.brickwork.users.service.contact.ContactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Override
    public ContactMessage submitMessage(ContactMessage message) {
        ContactMessage saved = contactMessageRepository.save(message);
        log.info("Contact message submitted: messageId={}, email={}", saved.getMessageId(), saved.getEmail());
        return saved;
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
                log.info("Contact message status updated: id={}, status={}", messageId, updates.get("status"));
            }
            return Optional.of(message);
        }
        log.warn("Contact message not found: messageId={}", messageId);
        return Optional.empty();
    }
}

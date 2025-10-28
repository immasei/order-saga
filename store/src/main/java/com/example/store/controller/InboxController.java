//package com.example.store.controller;
//
//import com.example.store.model.Inbox;
//import com.example.store.repository.InboxRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/inbox")
//public class InboxController {
//
//    @Autowired
//    private InboxRepository inboxRepository;
//
//    // Get inbox entry by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Inbox> getInboxById(@PathVariable UUID id) {
//        Optional<Inbox> inbox = inboxRepository.findById(id);
//        return inbox.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Get inbox entry by message ID
//    @GetMapping("/message/{messageId}")
//    public ResponseEntity<Inbox> getInboxByMessageId(@PathVariable String messageId) {
//        return inboxRepository.findAll()
//                .stream()
//                .filter(inbox -> inbox.getMessageId().equals(messageId))
//                .findFirst()
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Create a new inbox entry
//    @PostMapping
//    public ResponseEntity<Inbox> createInbox(@RequestBody Inbox inbox) {
//        Inbox createdInbox = inboxRepository.save(inbox);
//        return ResponseEntity.ok(createdInbox);
//    }
//
//    // Mark an inbox entry as processed
//    @PatchMapping("/{id}")
//    public ResponseEntity<Inbox> markAsProcessed(@PathVariable UUID id, @RequestBody Inbox inboxDetails) {
//        Optional<Inbox> inboxOptional = inboxRepository.findById(id);
//        if (inboxOptional.isPresent()) {
//            Inbox inbox = inboxOptional.get();
//            inbox.setProcessedAt(inboxDetails.getProcessedAt());
//            inboxRepository.save(inbox);
//            return ResponseEntity.ok(inbox);
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // Delete inbox entry by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteInbox(@PathVariable UUID id) {
//        if (inboxRepository.existsById(id)) {
//            inboxRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//}

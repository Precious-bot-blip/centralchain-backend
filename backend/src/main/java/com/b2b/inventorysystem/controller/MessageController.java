package com.b2b.inventorysystem.controller;

import com.b2b.inventorysystem.dto.message.MessageCreateRequest;
import com.b2b.inventorysystem.dto.message.MessageResponse;
import com.b2b.inventorysystem.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // Get all messages for a specific PO
    @GetMapping("/po/{poId}")
    public ResponseEntity<List<MessageResponse>> getMessagesForPo(@PathVariable Integer poId) {
        return ResponseEntity.ok(messageService.getMessagesForPo(poId));
    }

    // Send a new message
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageCreateRequest request) {
        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
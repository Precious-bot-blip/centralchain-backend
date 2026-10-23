package com.b2b.inventorysystem.service;

import com.b2b.inventorysystem.dto.message.MessageCreateRequest;
import com.b2b.inventorysystem.dto.message.MessageResponse;
import com.b2b.inventorysystem.entity.OrderMessage;
import com.b2b.inventorysystem.entity.PurchaseOrder;
import com.b2b.inventorysystem.entity.User;
import com.b2b.inventorysystem.exception.ResourceNotFoundException;
import com.b2b.inventorysystem.repository.OrderMessageRepository;
import com.b2b.inventorysystem.repository.PurchaseOrderRepository;
import com.b2b.inventorysystem.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final OrderMessageRepository messageRepository;
    private final PurchaseOrderRepository poRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(OrderMessageRepository messageRepository,
                          PurchaseOrderRepository poRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.poRepository = poRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // READ
    public List<MessageResponse> getMessagesForPo(Integer poId) {
        if (!poRepository.existsById(poId)) {
            throw new ResourceNotFoundException("Purchase Order not found: " + poId);
        }
        return messageRepository.findByPurchaseOrderPoIdOrderBySentAtAsc(poId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // SEND
    @Transactional
    public MessageResponse sendMessage(MessageCreateRequest request) {
        PurchaseOrder po = poRepository.findById(request.getPoId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + request.getPoId()));

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + request.getSenderId()));

        OrderMessage message = new OrderMessage();
        message.setPurchaseOrder(po);
        message.setSender(sender);
        message.setMessageText(request.getMessageText());

        OrderMessage saved = messageRepository.save(message);
        MessageResponse response = toResponse(saved);

        messagingTemplate.convertAndSend("/topic/orders/" + po.getPoId(), response);

        return response;
    }

    // HELPERS
    private MessageResponse toResponse(OrderMessage m) {
        return new MessageResponse(
                m.getMessageId(),
                m.getPurchaseOrder().getPoId(),
                m.getPurchaseOrder().getPoNumber(),
                m.getSender().getUserId(),
                m.getSender().getFullName(),
                m.getSender().getRole().getRoleName(),
                m.getMessageText(),
                m.getSentAt()
        );
    }
}
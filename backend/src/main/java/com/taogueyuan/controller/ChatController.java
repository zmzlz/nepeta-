package com.taogueyuan.controller;

import com.taogueyuan.dto.ContactDTO;
import com.taogueyuan.dto.SendMessageRequest;
import com.taogueyuan.entity.ChatMessage;
import com.taogueyuan.entity.User;
import com.taogueyuan.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(
                request.getSenderId(),
                request.getReceiverId(),
                request.getOrderId(),
                request.getContent(),
                request.getImageUrl()
        ));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ChatMessage>> getOrderMessages(@PathVariable Long orderId) {
        return ResponseEntity.ok(chatService.getOrderMessages(orderId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam Long userId1, @RequestParam Long userId2) {
        return ResponseEntity.ok(chatService.getChatHistory(userId1, userId2));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatMessage>> getUserMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getAllMessagesForUser(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChatMessage>> getAllMessages() {
        return ResponseEntity.ok(chatService.getAllMessages());
    }

    @PostMapping("/read/{messageId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        chatService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contacts/{userId}")
    public ResponseEntity<List<ContactDTO>> getRecentContacts(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getRecentContacts(userId));
    }
}

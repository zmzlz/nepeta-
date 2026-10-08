package com.taogueyuan.service;

import com.taogueyuan.dto.ContactDTO;
import com.taogueyuan.entity.ChatMessage;
import com.taogueyuan.entity.User;
import com.taogueyuan.entity.Order;
import com.taogueyuan.repository.ChatMessageRepository;
import com.taogueyuan.repository.UserRepository;
import com.taogueyuan.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ChatMessage sendMessage(Long senderId, Long receiverId, Long orderId, String content, String imageUrl) {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setImageUrl(imageUrl);
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);

        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            message.setOrder(order);
        }

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getOrderMessages(Long orderId) {
        return chatMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    public List<ChatMessage> getChatHistory(Long userId1, Long userId2) {
        return chatMessageRepository.findChatHistory(userId1, userId2);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        chatMessageRepository.findById(messageId).ifPresent(msg -> {
            msg.setRead(true);
            chatMessageRepository.save(msg);
        });
    }

    public List<ChatMessage> getAllMessagesForUser(Long userId) {
        return chatMessageRepository.findAllByUserId(userId);
    }

    public List<ChatMessage> getAllMessages() {
        return chatMessageRepository.findAll();
    }

    // Get recent chat contacts for a user (users who sent msg to me OR received msg from me)
    public List<ContactDTO> getRecentContacts(Long userId) {
        Set<Long> contactIds = new HashSet<>();
        contactIds.addAll(chatMessageRepository.findSendersByReceiverId(userId));
        contactIds.addAll(chatMessageRepository.findReceiversBySenderId(userId));
        
        // Remove self from contacts if any (shouldn't happen usually but good to be safe)
        contactIds.remove(userId);

        List<User> users = userRepository.findAllById(contactIds);
        
        return users.stream().map(contact -> {
            long unreadCount = chatMessageRepository.countBySenderIdAndReceiverIdAndIsReadFalse(contact.getId(), userId);
            List<ChatMessage> msgs = chatMessageRepository.findLatestMessages(userId, contact.getId());
            ChatMessage lastMsg = msgs.isEmpty() ? null : msgs.get(0);
            
            return new ContactDTO(
                contact, 
                (int) unreadCount, 
                lastMsg != null ? lastMsg.getContent() : "", 
                lastMsg != null ? lastMsg.getCreatedAt() : null
            );
        }).sorted((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        }).collect(Collectors.toList());
    }
}

package com.taogueyuan.repository;

import com.taogueyuan.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Get messages for a specific order
    List<ChatMessage> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    // Get messages between two users (sender and receiver)
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) ORDER BY m.createdAt ASC")
    List<ChatMessage> findChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Get unread count for a user
    long countByReceiverIdAndIsReadFalse(Long receiverId);
    
    // Get all messages involving a user
    @Query("SELECT m FROM ChatMessage m WHERE m.sender.id = :userId OR m.receiver.id = :userId ORDER BY m.createdAt DESC")
    List<ChatMessage> findAllByUserId(@Param("userId") Long userId);

    // Find distinct users who have chatted with a specific user (e.g. Admin)
    @Query("SELECT DISTINCT m.sender.id FROM ChatMessage m WHERE m.receiver.id = :userId")
    List<Long> findSendersByReceiverId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT m.receiver.id FROM ChatMessage m WHERE m.sender.id = :userId")
    List<Long> findReceiversBySenderId(@Param("userId") Long userId);

    long countBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}

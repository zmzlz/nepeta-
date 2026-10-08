package com.taogueyuan.dto;

import com.taogueyuan.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContactDTO {
    private Long id;
    private String nickname;
    private String avatar;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    
    public ContactDTO(User user, Integer unreadCount, String lastMessage, LocalDateTime lastMessageTime) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }
}

package com.taogueyuan.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long senderId;
    private Long receiverId;
    private Long orderId; // Optional
    private String content;
    private String imageUrl; // Optional
}

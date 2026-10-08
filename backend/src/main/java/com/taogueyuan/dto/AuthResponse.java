package com.taogueyuan.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String avatar;
    private String token; // For future JWT use, currently just a placeholder or session ID
}

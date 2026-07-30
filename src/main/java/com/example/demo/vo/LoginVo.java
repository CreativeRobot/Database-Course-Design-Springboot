package com.example.demo.vo;

import lombok.Data;
import com.example.demo.entity.Role;

@Data
public class LoginVo {
    private Long id;
    private String username;
    private String nickname;
    private Role role;
    private String token; // 返回给前端的 JWT Token
}

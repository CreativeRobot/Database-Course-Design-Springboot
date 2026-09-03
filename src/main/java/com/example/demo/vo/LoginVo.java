package com.example.demo.vo;

import lombok.Data;
import com.example.demo.entity.Role;

/**
 * LoginVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class LoginVo {
    private Long id;
    private String username;
    private String nickname;
    private Role role;
    private String token; // 返回给前端的 JWT Token
}

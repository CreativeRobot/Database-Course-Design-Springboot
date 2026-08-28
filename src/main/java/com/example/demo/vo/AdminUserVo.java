package com.example.demo.vo;

import com.example.demo.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;

/** 管理端用户视图，不包含密码等敏感字段。 */
@Data
public class AdminUserVo {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private Role role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.common.utils.JwtUtils;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.vo.LoginVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        return Result.success(createLoginVo(user));
    }

    @PostMapping("/register")
    public Result<LoginVo> register(@Valid @RequestBody RegisterDTO registerDTO) {
        User user = userService.register(registerDTO);
        return Result.success(createLoginVo(user));
    }

    private LoginVo createLoginVo(User user) {
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        LoginVo loginVO = new LoginVo();
        loginVO.setId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setRole(user.getRole());
        loginVO.setToken(token);
        return loginVO;
    }
}
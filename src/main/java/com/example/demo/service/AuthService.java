package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.utils.JwtUtils;
import com.example.demo.common.utils.UsernameUtils;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional(readOnly = true)
    public LoginVo login(LoginDTO loginDTO) {
        if (loginDTO == null
                || !StringUtils.hasText(loginDTO.getUsername())
                || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名或密码不能为空");
        }

        String username = UsernameUtils.normalize(loginDTO.getUsername());
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED,
                        "用户名或密码错误"
                ));

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "该账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        return createLoginVo(user);
    }

    @Transactional
    public LoginVo register(RegisterDTO registerDTO) {
        if (registerDTO == null
                || !StringUtils.hasText(registerDTO.getUsername())
                || !StringUtils.hasText(registerDTO.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名或密码不能为空");
        }

        String username = UsernameUtils.normalize(registerDTO.getUsername());
        String password = registerDTO.getPassword();
        if (username.length() < 3 || username.length() > 30) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名长度必须为3到30个字符");
        }
        if (password.length() < 6 || password.length() > 50) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码长度必须为6到50个字符");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(1);
        user.setRole(Role.CUSTOMER);
        user.setNickname(trimToNull(registerDTO.getNickname()));
        user.setEmail(trimToNull(registerDTO.getEmail()));
        user.setPhone(trimToNull(registerDTO.getPhone()));
        return createLoginVo(userRepository.save(user));
    }

    private LoginVo createLoginVo(User user) {
        LoginVo loginVo = new LoginVo();
        loginVo.setId(user.getId());
        loginVo.setUsername(user.getUsername());
        loginVo.setNickname(user.getNickname());
        loginVo.setRole(user.getRole());
        loginVo.setToken(jwtUtils.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        ));
        return loginVo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

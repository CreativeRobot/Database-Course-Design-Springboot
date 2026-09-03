package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.utils.JwtUtils;
import com.example.demo.common.utils.UsernameUtils;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.ForgotPasswordDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.SecurityQuestionVo;
import com.example.demo.service.SecurityQuestionService;
import com.example.demo.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 认证业务服务，负责用户注册、登录和会话相关处理。
 */
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private SecurityQuestionService securityQuestionService;

    // ==================== 业务方法 ====================

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    @Transactional(readOnly = true)
    public LoginVo login(LoginDTO loginDTO) {
        if (loginDTO == null
                || !StringUtils.hasText(loginDTO.getUsername())
                || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名或密码不能为空");
        }
        verifyLoginCaptcha(loginDTO);

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

    /**
     * 创建并保存当前业务数据。
     */
    @Transactional
    public LoginVo register(RegisterDTO registerDTO) {
        if (registerDTO == null
                || !StringUtils.hasText(registerDTO.getUsername())
                || !StringUtils.hasText(registerDTO.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名或密码不能为空");
        }
        captchaService.verifyAndConsume(registerDTO.getCaptchaId(), registerDTO.getCaptchaCode());

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
        User saved = userRepository.save(user);
        securityQuestionService.replace(saved.getId(), saved, registerDTO.getSecurityQuestions());
        return createLoginVo(saved);
    }

    @Transactional(readOnly = true)
    public List<SecurityQuestionVo> securityQuestions(String username) {
        if (!StringUtils.hasText(username)) throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        User user = userRepository.findByUsernameIgnoreCase(UsernameUtils.normalize(username))
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在或未设置密保问题"));
        List<SecurityQuestionVo> questions = securityQuestionService.questionsForUser(user.getId());
        if (questions.size() < 3) throw new BusinessException(HttpStatus.BAD_REQUEST, "该账户未设置密保问题，无法找回密码");
        return questions;
    }

    @Transactional
    public void forgotPassword(ForgotPasswordDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUsername())) throw new BusinessException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        if (!StringUtils.hasText(dto.getNewPassword()) || !dto.getNewPassword().equals(dto.getConfirmPassword())) throw new BusinessException(HttpStatus.BAD_REQUEST, "新密码不能为空且两次输入必须一致");
        User user = userRepository.findByUsernameIgnoreCase(UsernameUtils.normalize(dto.getUsername()))
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "密保答案不正确"));
        securityQuestionService.verify(user.getId(), dto.getAnswers());
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 创建并保存当前业务数据。
     */
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

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    private void verifyLoginCaptcha(LoginDTO loginDTO) {
        boolean hasId = StringUtils.hasText(loginDTO.getCaptchaId());
        boolean hasCode = StringUtils.hasText(loginDTO.getCaptchaCode());
        if (hasId != hasCode) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请完整填写验证码");
        }
        if (hasId) {
            captchaService.verifyAndConsume(loginDTO.getCaptchaId(), loginDTO.getCaptchaCode());
        }
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

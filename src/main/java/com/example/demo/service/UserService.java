package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.UpdateProfileDTO;
import com.example.demo.dto.UpdateSecurityQuestionsDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.UploadFileVo;
import com.example.demo.vo.UserProfileVo;
import com.example.demo.vo.SecurityQuestionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * UserService 业务服务，封装相关领域的业务规则和数据访问流程。
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SecurityQuestionService securityQuestionService;

    // ==================== 业务方法 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @Transactional(readOnly = true)
    public UserProfileVo getProfile(Long userId) {
        return toProfileVo(getActiveUser(userId));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @Transactional
    public UserProfileVo updateProfile(Long userId, UpdateProfileDTO updateProfileDTO) {
        if (updateProfileDTO == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户资料不能为空");
        }

        User user = getActiveUser(userId);
        user.setNickname(trimToNull(updateProfileDTO.getNickname()));
        user.setEmail(trimToNull(updateProfileDTO.getEmail()));
        user.setPhone(trimToNull(updateProfileDTO.getPhone()));
        return toProfileVo(userRepository.save(user));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @Transactional
    public UserProfileVo updateAvatar(Long userId, MultipartFile file) {
        User user = getActiveUser(userId);
        UploadFileVo upload = fileStorageService.storeAvatar(user.getId(), file);
        user.setAvatarUrl(upload.getUrl());
        return toProfileVo(userRepository.save(user));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        if (changePasswordDTO == null
                || !StringUtils.hasText(changePasswordDTO.getOldPassword())
                || !StringUtils.hasText(changePasswordDTO.getNewPassword())
                || !StringUtils.hasText(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "密码不能为空");
        }
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "两次输入的新密码不一致");
        }

        User user = getActiveUser(userId);
        if (securityQuestionService.questionsForUser(userId).size() < 3) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请先设置三个密保问题后再修改密码");
        }
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "原密码错误");
        }
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "新密码不能与原密码相同");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<SecurityQuestionVo> getSecurityQuestions(Long userId) {
        getActiveUser(userId);
        return securityQuestionService.questionsForUser(userId);
    }

    @Transactional
    public List<SecurityQuestionVo> updateSecurityQuestions(Long userId, UpdateSecurityQuestionsDTO dto) {
        User user = getActiveUser(userId);
        if (dto == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "密保问题不能为空");
        securityQuestionService.replace(userId, user, dto.getQuestions());
        return securityQuestionService.questionsForUser(userId);
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    private User getActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        return userRepository.findByIdAndStatus(userId, 1)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "用户不存在或已被禁用"
                ));
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    private UserProfileVo toProfileVo(User user) {
        UserProfileVo userProfileVo = new UserProfileVo();
        userProfileVo.setId(user.getId());
        userProfileVo.setUsername(user.getUsername());
        userProfileVo.setNickname(user.getNickname());
        userProfileVo.setEmail(user.getEmail());
        userProfileVo.setPhone(user.getPhone());
        userProfileVo.setAvatarUrl(user.getAvatarUrl());
        userProfileVo.setRole(user.getRole());
        userProfileVo.setStatus(user.getStatus());
        userProfileVo.setSecurityQuestionsConfigured(securityQuestionService.questionsForUser(user.getId()).size() == 3);
        userProfileVo.setCreateTime(user.getCreateTime());
        userProfileVo.setUpdateTime(user.getUpdateTime());
        return userProfileVo;
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

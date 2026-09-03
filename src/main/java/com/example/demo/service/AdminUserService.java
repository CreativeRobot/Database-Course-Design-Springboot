package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.AdminUserVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 管理端用户管理业务。 */
@Service
public class AdminUserService {

    private static final int ACTIVE = 1;
    private static final int INACTIVE = 0;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private UserRepository userRepository;

    // ==================== 业务方法 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @Transactional(readOnly = true)
    public PageVo<AdminUserVo> listUsers(
            String keyword, Integer status, Role role, int page, int size) {
        validatePage(page, size);
        validateStatus(status);
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id")));
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<User> users = userRepository.searchForAdmin(
                normalizedKeyword, status, role, pageable);
        return PageVo.of(users.map(this::toVo));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @Transactional(readOnly = true)
    public AdminUserVo getUser(Long userId) {
        return toVo(getUserOrThrow(userId));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @Transactional
    public AdminUserVo changeStatus(Long operatorId, Long userId, Integer status) {
        validateStatus(status);
        if (operatorId == null || userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户不能为空");
        }
        if (operatorId.equals(userId) && status == INACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能禁用当前登录管理员");
        }

        User user = getUserOrThrow(userId);
        if (user.getRole() == Role.ADMIN
                && user.getStatus() == ACTIVE
                && status == INACTIVE
                && userRepository.countByRoleAndStatus(Role.ADMIN, ACTIVE) <= 1) {
            throw new BusinessException(HttpStatus.CONFLICT, "系统至少需要保留一个有效管理员");
        }

        user.setStatus(status);
        return toVo(userRepository.save(user));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户不能为空");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    private void validateStatus(Integer status) {
        if (status != null && status != ACTIVE && status != INACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户状态只能为0或1");
        }
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    private void validatePage(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "页码必须从1开始，每页数量必须在1到" + MAX_PAGE_SIZE + "之间");
        }
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    private AdminUserVo toVo(User user) {
        AdminUserVo vo = new AdminUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}


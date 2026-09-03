package com.example.demo.common.utils;

import java.util.Locale;

    // ==================== 公共方法 ====================

/**
 * UsernameUtils 公共组件，提供后端各模块共享的基础能力。
 */
    /**
     * 执行当前模块的业务处理逻辑。
     */
public final class UsernameUtils {
    private UsernameUtils() {
    }
    /**
     * 不被继承
     * 仅通过静态方法调用
     * 统一处理输入的用户名转化为小写且去除空格
     * */
    public static String normalize(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }
}
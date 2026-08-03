package com.example.demo.common.utils;

import java.util.Locale;

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
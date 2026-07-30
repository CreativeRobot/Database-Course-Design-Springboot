package com.example.demo.common.utils;

import java.util.Locale;

public final class UsernameUtils {
    private UsernameUtils() {
    }

    public static String normalize(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }
}
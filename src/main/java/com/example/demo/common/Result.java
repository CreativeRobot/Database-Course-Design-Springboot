package com.example.demo.common;

import lombok.Data;

/**
 * Result 公共组件，提供后端各模块共享的基础能力。
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // ==================== 公共方法 ====================

    /**
     * 执行当前模块的业务处理逻辑。
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
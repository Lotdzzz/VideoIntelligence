package com.framework.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author dotm
 * 接口响应结果
 *
 * @param <T> 响应数据类型
 */
public class Result<T> extends HashMap<String, Object> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    public static final String CODE = "code";

    /**
     * 返回消息
     */
    public static final String MESSAGE = "msg";

    /**
     * 数据
     */
    public static final String DATA = "data";

    /**
     * 成功消息
     */
    public static final String SUCCESS = "success";

    public Result() {
    }

    public Result(int code, String message, T data) {
        super.put(CODE, code);
        super.put(MESSAGE, message);
        super.put(DATA, data);
    }

    /**
     * 设置数据
     */
    public void setData(T data) {
        super.put(DATA, data);
    }

    /**
     * 添加自定义字段
     *
     * 返回自身，支持链式调用
     */
    public Result<T> putData(String key, Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * 成功，无数据
     */
    public static <T> Result<T> success() {
        return new Result<>(200, SUCCESS, null);
    }

    /**
     * 成功，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, SUCCESS, data);
    }

    /**
     * 成功，自定义消息
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败，自定义状态码和消息
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
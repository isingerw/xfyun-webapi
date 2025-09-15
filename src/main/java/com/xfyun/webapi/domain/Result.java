package com.xfyun.webapi.domain;

import java.util.HashMap;

/**
 * 通用返回结果类
 * 
 * @param <T> 返回数据的类型
 * @author xfyun-webapi
 * @version 1.0
 * @since 2025-09-15
 */
public class Result<T> extends HashMap<String, Object> {

    private static final String ATTR_ERROR_CODE = "errorCode";
    private static final String ATTR_MESSAGE = "message";
    private static final String ATTR_DATA = "data";

    private Result(int errorCode) {
        put(ATTR_ERROR_CODE, errorCode);
    }

    public static <T> Result<T> success() {
        return new Result<>(0);
    }

    public static <T> Result<T> create(boolean success) {
        if (success) {
            return success();
        } else {
            return fail(1);
        }
    }

    public static <T> Result<T> success(T data) {
        Result<T> ret = new Result<>(0);
        ret.put(ATTR_DATA, data);
        return ret;
    }

    public static <T> Result<T> success(String key, Object value) {
        Result<T> ret = new Result<>(0);
        ret.put(key, value);
        return ret;
    }

    public static <T> Result<T> fail() {
        return fail(1);
    }

    public static <T> Result<T> fail(int errorCode) {
        return new Result<>(errorCode);
    }

    public static <T> Result<T> fail(int errorCode, String failMessage) {
        Result<T> result = fail(errorCode);
        result.put(ATTR_MESSAGE, failMessage);
        return result;
    }

    public int failCode() {
        return get(ATTR_ERROR_CODE);
    }

    public void failCode(int errorCode) {
        this.put(ATTR_ERROR_CODE, errorCode);
    }

    public String failMessage() {
        return this.get(ATTR_MESSAGE);
    }

    public void failMessage(String errorMessage) {
        put(ATTR_MESSAGE, errorMessage);
    }

    public T data() {
        return this.get(ATTR_DATA);
    }

    public void data(T data) {
        put(ATTR_DATA, data);
    }

    @SuppressWarnings("unchecked")
    public <R> R get(String key) {
        return (R) super.get(key);
    }

    public Result<T> set(String key, Object value) {
        put(key, value);
        return this;
    }

    public Result<T> setIfNotNull(String key, Object value) {
        if (value != null && key != null) {
            put(key, value);
        }
        return this;
    }

    public boolean isSuccess() {
        return failCode() == 0;
    }
}

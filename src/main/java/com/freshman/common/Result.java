package com.freshman.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 * 功能：封装API接口的统一返回格式，包含状态码、消息提示和返回数据，
 *       前端根据此格式统一处理成功和失败的响应
 * 负责成员：D (Team Lead)
 * 所属模块：通用工具模块
 *
 * @param <T> 响应数据的泛型类型
 * @author D (Team Lead)
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码：200表示成功，500表示服务器内部错误 */
    private Integer code;

    /** 响应消息，用于前端提示用户 */
    private String message;

    /** 响应数据，泛型类型，可以是任意数据类型 */
    private T data;

    /**
     * 返回成功结果（无数据）
     *
     * @param <T> 泛型类型
     * @return 状态码200、成功消息、无数据的Result对象
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 返回成功结果（带数据）
     *
     * @param data 响应数据
     * @param <T>  泛型类型
     * @return 状态码200、成功消息、带数据的Result对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 返回成功结果（自定义消息和带数据）
     *
     * @param message 自定义成功消息
     * @param data    响应数据
     * @param <T>     泛型类型
     * @return 状态码200、自定义消息、带数据的Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 返回失败结果（使用默认错误码500）
     *
     * @param message 错误消息
     * @param <T>     泛型类型
     * @return 状态码500、错误消息、无数据的Result对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 返回失败结果（自定义错误码）
     *
     * @param code    自定义错误状态码
     * @param message 错误消息
     * @param <T>     泛型类型
     * @return 自定义状态码、错误消息、无数据的Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}

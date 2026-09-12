package com.freshman.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freshman.entity.User;

/**
 * 用户服务接口
 * 功能：定义用户相关的业务逻辑接口，继承MyBatis-Plus的IService获得通用CRUD方法，
 *       并扩展了用户注册、用户名查询、登录时间更新等自定义业务方法
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 用户模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * 功能：验证用户名唯一性，加密密码后保存用户信息到数据库
     *
     * @param user 待注册的用户实体（需包含用户名和密码）
     * @return true-注册成功 false-注册失败（如用户名已存在）
     */
    boolean register(User user);

    /**
     * 根据用户名查询用户
     * 功能：用于登录认证、用户信息展示等场景
     *
     * @param username 用户名（登录名）
     * @return 查询到的用户实体，如果用户不存在则返回null
     */
    User findByUsername(String username);

    /**
     * 更新用户最后登录时间
     * 功能：用户成功登录后，将其最后登录时间更新为当前时间
     *
     * @param userId 用户ID
     */
    void updateLastLoginTime(Long userId);
}

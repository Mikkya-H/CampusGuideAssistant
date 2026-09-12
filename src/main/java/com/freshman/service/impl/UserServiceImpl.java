package com.freshman.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freshman.entity.User;
import com.freshman.entity.UserRole;
import com.freshman.mapper.UserMapper;
import com.freshman.mapper.UserRoleMapper;
import com.freshman.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 功能：实现用户相关的业务逻辑，包括：
 *       1. 用户注册（用户名唯一性检查 + BCrypt密码加密 + 角色分配）
 *       2. 根据用户名查询用户
 *       3. 更新用户最后登录时间
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 用户模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;

    /**
     * 构造器注入PasswordEncoder和UserRoleMapper
     * @param passwordEncoder BCrypt密码编码器，用于密码加密
     * @param userRoleMapper 用户角色关联数据访问层
     */
    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRoleMapper userRoleMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userRoleMapper = userRoleMapper;
    }

    /**
     * 用户注册
     * 流程：1. 检查用户名是否已被占用 2. 使用BCrypt加密密码 3. 设置默认状态为正常 4. 保存到数据库 5. 分配学生角色
     *
     * @param user 待注册的用户实体
     * @return true-注册成功 false-用户名已存在导致注册失败
     */
    @Override
    @Transactional
    public boolean register(User user) {
        // 检查用户名是否已存在，保证用户名唯一性
        if (findByUsername(user.getUsername()) != null) {
            return false;
        }
        // 使用BCrypt算法对密码进行不可逆加密，确保密码安全存储
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置用户状态为正常（1-正常）
        user.setStatus(1);
        // 调用MyBatis-Plus的save方法将用户信息写入数据库
        boolean saved = save(user);
        if (saved) {
            // 分配默认学生角色（role_id=2 对应 ROLE_STUDENT）
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(2L);
            userRoleMapper.insert(userRole);
        }
        return saved;
    }

    /**
     * 根据用户名查询用户
     * 功能：使用LambdaQueryWrapper构建查询条件，精确匹配用户名
     *
     * @param username 用户名，为空或空白时直接返回null
     * @return 查询到的用户实体，不存在则返回null
     */
    @Override
    public User findByUsername(String username) {
        // 参数校验：用户名为空或纯空白字符串时直接返回null
        if (StrUtil.isBlank(username)) {
            return null;
        }
        // 构建Lambda查询条件：精确匹配用户名
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        // 执行查询，返回单条记录
        return getOne(wrapper);
    }

    /**
     * 更新用户最后登录时间
     * 功能：将指定用户的最后登录时间更新为当前时间
     *
     * @param userId 用户ID
     */
    @Override
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        // 设置最后登录时间为当前系统时间
        user.setLastLoginTime(LocalDateTime.now());
        // 只更新最后登录时间字段，不影响其他字段
        updateById(user);
    }
}

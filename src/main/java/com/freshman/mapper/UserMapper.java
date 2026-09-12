package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户数据访问层接口
 * 功能：提供对 sys_user 表的CRUD操作，继承MyBatis-Plus的BaseMapper获得通用数据库操作方法
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 用户模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供，无需额外定义

    /**
     * 查询所有用户及其角色名称
     * 功能：LEFT JOIN 三张表（sys_user、sys_user_role、sys_role），
     *       获取每个用户及其对应的角色名称，未分配角色的用户roleName为null
     * @return 带有角色名称的用户列表
     */
    @Select("SELECT u.*, r.role_name AS roleName FROM sys_user u " +
            "LEFT JOIN sys_user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE u.deleted = 0 " +
            "ORDER BY u.create_time DESC")
    List<User> findAllWithRoles();
}

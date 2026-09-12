package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联数据访问层接口
 * 功能：提供对用户与角色关联关系的数据库操作，用于实现用户多角色管理
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 权限模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}
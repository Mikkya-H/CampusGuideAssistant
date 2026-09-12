package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问层接口
 * 功能：提供对 sys_role 表的CRUD操作，继承MyBatis-Plus的BaseMapper获得通用数据库操作方法
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 权限模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

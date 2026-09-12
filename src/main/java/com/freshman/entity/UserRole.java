package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * 用户角色关联实体类
 * 功能：记录用户与角色的关联关系，一个用户可以有多个角色
 * 对应数据库表：sys_user_role
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 权限模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Data
@TableName("sys_user_role")
public class UserRole {

    /** 关联ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;
}

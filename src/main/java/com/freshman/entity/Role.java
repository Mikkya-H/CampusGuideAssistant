package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * 功能：定义系统角色信息，包括角色编码、角色名称、描述等，用于权限控制
 * 对应数据库表：sys_role
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 权限模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Data
@TableName("sys_role")
public class Role {

    /** 角色ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，如 ROLE_ADMIN、ROLE_STUDENT */
    private String roleCode;

    /** 角色名称，如"管理员"、"学生" */
    private String roleName;

    /** 角色描述，说明该角色的职责和权限范围 */
    private String description;

    /** 排序字段，数值越小越靠前 */
    private Integer sort;

    /** 状态：0-禁用 1-正常 */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

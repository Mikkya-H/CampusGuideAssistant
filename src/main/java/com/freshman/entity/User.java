package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 功能：定义系统用户的核心信息，包括基本资料、学籍信息、账户状态等
 * 对应数据库表：sys_user
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 用户模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Data
@TableName("sys_user")
public class User {

    /** 用户ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，唯一标识 */
    private String username;

    /** 登录密码，使用BCrypt算法加密存储 */
    private String password;

    /** 用户真实姓名，用于页面展示 */
    private String realName;

    /** 学号，新生入学后的唯一学籍编号 */
    private String studentNo;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 手机号码，用于联系和短信通知 */
    private String phone;

    /** 电子邮箱，用于邮件通知 */
    private String email;

    /** 所属学院，如"计算机学院" */
    private String college;

    /** 专业名称 */
    private String majorName;

    /** 年级，如"2024" */
    private String grade;

    /** 头像图片URL地址 */
    private String avatar;

    /** 账户状态：0-已禁用 1-正常使用 */
    private Integer status;

    /** 最后一次登录系统的时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间，插入数据时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新数据时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0-未删除 1-已删除（MyBatis-Plus逻辑删除） */
    @TableLogic
    private Integer deleted;

    /** 角色名称（非数据库字段，用于页面展示） */
    @TableField(exist = false)
    private String roleName;
}

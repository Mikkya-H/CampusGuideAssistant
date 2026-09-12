package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教师信息实体类
 * 功能：展示各专业教师的基本信息，包括职称、研究方向、个人简介等，方便新生了解师资力量
 * 对应数据库表：guide_teacher
 * 负责成员：W
 * 所属模块：迎新指南 / 教师名录模块
 *
 * @author W
 * @version 1.0
 */
@Data
@TableName("guide_teacher")
public class Teacher {

    /** 教师ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 教师姓名 */
    private String name;

    /** 职称，如"教授"、"副教授"、"讲师" */
    private String title;

    /** 所属学院 */
    private String college;

    /** 关联专业ID，外键关联guide_major表 */
    private Long majorId;

    /** 教师照片URL */
    private String photoUrl;

    /** 教师邮箱 */
    private String email;

    /** 研究方向，如"人工智能"、"大数据" */
    private String researchDirection;

    /** 个人简介，详细介绍教师的学术背景和成果 */
    private String introduction;

    /** 排序字段，数值越小越靠前 */
    private Integer sort;

    /** 状态：0-隐藏 1-显示 */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

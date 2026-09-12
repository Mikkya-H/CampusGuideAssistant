package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 专业信息实体类
 * 功能：展示各专业的详细信息，包括学位类型、学制、课程设置、就业前景等，帮助新生了解专业
 * 对应数据库表：guide_major
 * 负责成员：W
 * 所属模块：迎新指南 / 专业介绍模块
 *
 * @author W
 * @version 1.0
 */
@Data
@TableName("guide_major")
public class Major {

    /** 专业ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 专业名称，如"计算机科学与技术" */
    private String name;

    /** 所属学院，如"计算机学院" */
    private String college;

    /** 专业代码，教育部的标准专业编号 */
    private String code;

    /** 学位类型，如"工学学士"、"理学学士" */
    private String degree;

    /** 学制（年），如4表示四年制本科 */
    private Integer duration;

    /** 专业描述，详细介绍专业的培养目标和方向 */
    private String description;

    /** 主要课程，列出该专业的核心课程 */
    private String courses;

    /** 就业前景，说明该专业的就业方向和前景分析 */
    private String careerProspect;

    /** 专业特色，介绍该专业的独特优势和亮点 */
    private String features;

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

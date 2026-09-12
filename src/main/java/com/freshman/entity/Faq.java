package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 常见问题(FAQ)实体类
 * 功能：存储迎新相关的常见问题及解答，支持分类浏览和浏览量统计
 * 对应数据库表：guide_faq
 * 负责成员：W
 * 所属模块：迎新指南 / 常见问题模块
 *
 * @author W
 * @version 1.0
 */
@Data
@TableName("guide_faq")
public class Faq {

    /** 问题ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 问题内容 */
    private String question;

    /** 问题答案 */
    private String answer;

    /** 问题分类，如"报到流程"、"住宿问题"、"缴费问题" */
    private String category;

    /** 浏览次数，用于统计热门问题 */
    private Integer viewCount;

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

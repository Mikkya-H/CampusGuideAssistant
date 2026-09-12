package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新闻公告实体类
 * 功能：管理系统中的新闻和公告信息，支持置顶、分类、浏览量统计等
 * 对应数据库表：sys_news
 * 负责成员：Z
 * 所属模块：新闻公告模块
 *
 * @author Z
 * @version 1.0
 */
@Data
@TableName("sys_news")
public class News {

    /** 新闻ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 新闻标题 */
    private String title;

    /** 新闻摘要，用于列表页展示 */
    private String summary;

    /** 新闻正文内容，富文本格式 */
    private String content;

    /** 封面图片URL */
    private String coverImage;

    /** 新闻分类，如"学校通知"、"迎新资讯"、"校园动态" */
    private String category;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 浏览量，记录新闻被查看的次数 */
    private Integer viewCount;

    /** 发布者姓名 */
    private String author;

    /** 状态：0-草稿 1-已发布 */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 * 功能：定义交流社区中的帖子信息，支持分类浏览、精华置顶、浏览量统计和点赞功能
 * 对应数据库表：forum_post
 * 负责成员：S
 * 所属模块：交流社区 / 论坛模块
 *
 * @author S
 * @version 1.0
 */
@Data
@TableName("forum_post")
public class ForumPost {

    /** 帖子ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子标题 */
    private String title;

    /** 帖子正文内容 */
    private String content;

    /** 发帖用户ID，外键关联sys_user表 */
    private Long userId;

    /** 帖子分类，如"学习交流"、"生活咨询"、"社团纳新" */
    private String category;

    /** 是否精华帖：0-否 1-是 */
    private Integer isEssence;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 浏览量，记录帖子被查看的次数 */
    private Integer viewCount;

    /** 回复数量，记录帖子下的评论总数 */
    private Integer replyCount;

    /** 点赞数量 */
    private Integer likeCount;

    /** 状态：0-已删除 1-正常显示 */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 发帖用户名（非数据库字段，用于页面展示） */
    @TableField(exist = false)
    private String username;

    /** 发帖用户头像URL（非数据库字段，用于页面展示） */
    @TableField(exist = false)
    private String userAvatar;
}

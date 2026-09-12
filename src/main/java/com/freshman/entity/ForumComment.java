package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛评论实体类
 * 功能：定义论坛帖子下的评论信息，支持回复评论（通过parentId实现嵌套回复）
 * 对应数据库表：forum_comment
 * 负责成员：S
 * 所属模块：交流社区 / 论坛模块
 *
 * @author S
 * @version 1.0
 */
@Data
@TableName("forum_comment")
public class ForumComment {

    /** 评论ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属帖子ID，外键关联forum_post表 */
    private Long postId;

    /** 评论用户ID，外键关联sys_user表 */
    private Long userId;

    /** 父评论ID：为null表示直接评论帖子，不为null表示回复某条评论 */
    private Long parentId;

    /** 评论内容 */
    private String content;

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

    /** 评论用户名（非数据库字段，用于页面展示） */
    @TableField(exist = false)
    private String username;

    /** 被回复的用户名（非数据库字段，用于页面展示"回复 @xxx"） */
    @TableField(exist = false)
    private String parentUsername;
}

package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 校园活动实体类
 * 功能：展示学校举办的各类校园活动信息，包括活动时间、地点、参与人数等，方便新生参与校园活动
 * 对应数据库表：life_activity
 * 负责成员：S
 * 所属模块：校园生活 / 校园活动模块
 *
 * @author S
 * @version 1.0
 */
@Data
@TableName("life_activity")
public class Activity {

    /** 活动ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动标题 */
    private String title;

    /** 活动描述，详细介绍活动内容和形式 */
    private String description;

    /** 活动分类，如"学术讲座"、"文艺演出"、"体育比赛"、"志愿服务" */
    private String category;

    /** 活动地点 */
    private String location;

    /** 主办方/组织者 */
    private String organizer;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 最大参与人数限制 */
    private Integer maxParticipants;

    /** 当前已报名人数 */
    private Integer currentParticipants;

    /** 活动封面图片URL */
    private String coverImage;

    /** 联系方式，用于活动咨询 */
    private String contact;

    /** 状态：0-未开始 1-进行中 2-已结束 */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

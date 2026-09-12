package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社团信息实体类
 * 功能：展示学校各类社团的基本信息，包括社团类型、负责人、纳新信息等，帮助新生选择加入的社团
 * 对应数据库表：life_club
 * 负责成员：S
 * 所属模块：校园生活 / 社团信息模块
 *
 * @author S
 * @version 1.0
 */
@Data
@TableName("life_club")
public class Club {

    /** 社团ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 社团名称 */
    private String name;

    /** 社团分类，如"学术科技"、"文化艺术"、"体育竞技"、"志愿服务" */
    private String category;

    /** 社团描述，介绍社团的宗旨和主要活动内容 */
    private String description;

    /** 社团Logo图片URL */
    private String logoUrl;

    /** 社长/负责人姓名 */
    private String president;

    /** 当前成员数量 */
    private Integer memberCount;

    /** 纳新信息，说明纳新条件和方式 */
    private String recruitInfo;

    /** 活动时间，如"每周三晚19:00" */
    private String activityTime;

    /** 活动地点 */
    private String location;

    /** 联系方式，如手机号或QQ群号 */
    private String contact;

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

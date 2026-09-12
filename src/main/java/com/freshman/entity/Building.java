package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 校园建筑/地标实体类
 * 功能：记录校园内各类建筑的详细信息，包括教学楼、图书馆、体育馆等的位置和属性
 * 对应数据库表：campus_building
 * 负责成员：Z
 * 所属模块：校园导览模块
 *
 * @author Z
 * @version 1.0
 */
@Data
@TableName("campus_building")
public class Building {

    /** 建筑ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 建筑名称，如"图书馆"、"第一教学楼" */
    private String name;

    /** 建筑分类，如"教学楼"、"生活设施"、"体育场馆" */
    private String category;

    /** 建筑描述，详细介绍建筑的功能和特点 */
    private String description;

    /** 建筑具体地址 */
    private String address;

    /** 经度坐标，用于地图定位 */
    private Double longitude;

    /** 纬度坐标，用于地图定位 */
    private Double latitude;

    /** 建筑楼层数 */
    private Integer floors;

    /** 开放时间，如"08:00-22:00" */
    private String openingHours;

    /** 建筑图片URL */
    private String imageUrl;

    /** 标签，多个标签用逗号分隔，如"自习,借书,打印" */
    private String tags;

    /** 排序字段，数值越小越靠前 */
    private Integer sort;

    /** 状态：0-禁用 1-正常展示 */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

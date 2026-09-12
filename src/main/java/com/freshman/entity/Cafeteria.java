package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 食堂信息实体类
 * 功能：展示学校各食堂的基本信息，包括位置、楼层、特色菜品等，方便新生了解就餐选择
 * 对应数据库表：life_cafeteria
 * 负责成员：S
 * 所属模块：校园生活 / 食堂信息模块
 *
 * @author S
 * @version 1.0
 */
@Data
@TableName("life_cafeteria")
public class Cafeteria {

    /** 食堂ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 食堂名称，如"第一食堂"、"风味餐厅" */
    private String name;

    /** 食堂位置，描述食堂所在的具体位置 */
    private String location;

    /** 楼层数 */
    private Integer floors;

    /** 营业时间，如"06:30-21:00" */
    private String openingHours;

    /** 食堂描述，介绍食堂的整体环境和风格 */
    private String description;

    /** 特色菜品，推荐该食堂的招牌美食 */
    private String specialties;

    /** 食堂图片URL */
    private String imageUrl;

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

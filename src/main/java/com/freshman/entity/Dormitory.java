package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宿舍信息实体类
 * 功能：展示学校各宿舍楼的信息，包括房型、设施、费用等，帮助新生了解住宿条件
 * 对应数据库表：life_dormitory
 * 负责成员：S
 * 所属模块：校园生活 / 宿舍信息模块
 *
 * @author S
 * @version 1.0
 */
@Data
@TableName("life_dormitory")
public class Dormitory {

    /** 宿舍ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宿舍楼名称，如"学生公寓1号楼" */
    private String name;

    /** 宿舍楼编号 */
    private String buildingNo;

    /** 宿舍类型，如"男生宿舍"、"女生宿舍" */
    private String type;

    /** 房间类型，如"四人间"、"六人间"、"套间" */
    private String roomType;

    /** 配套设施，如"空调、热水器、独立卫生间" */
    private String facilities;

    /** 宿舍描述，详细介绍宿舍环境 */
    private String description;

    /** 住宿费用（元/学年） */
    private BigDecimal fee;

    /** 宿舍图片URL */
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

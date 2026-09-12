package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报到流程步骤实体类
 * 功能：定义新生报到注册的各环节信息，包括步骤编号、办理地点、所需材料、注意事项等
 * 对应数据库表：guide_registration_step
 * 负责成员：W
 * 所属模块：迎新指南 / 报到流程模块
 *
 * @author W
 * @version 1.0
 */
@Data
@TableName("guide_registration_step")
public class RegistrationStep {

    /** 步骤ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 步骤序号，如1、2、3，表示报到流程的执行顺序 */
    private Integer stepNo;

    /** 步骤标题，如"身份验证"、"缴纳学费" */
    private String title;

    /** 步骤详细描述，说明该步骤的具体内容和要求 */
    private String description;

    /** 办理地点，如"行政楼一楼大厅" */
    private String location;

    /** 所需材料清单，如"身份证、录取通知书" */
    private String requiredMaterials;

    /** 温馨提示/注意事项 */
    private String tips;

    /** 步骤图标CSS类名或图片URL */
    private String icon;

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

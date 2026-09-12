package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 知识库实体类
 * 功能：存储迎新知识库条目，每条包含标准问题、答案、分类、关键词和同义词，
 *       是AI智能问答机器人的核心数据源
 *
 * 所属模块：AI 智能问答模块
 * @author AI Module Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_knowledge")
public class AiKnowledge {

    /** 知识条目ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标准问题/问法示例 */
    private String question;

    /** 答案内容（支持多段落、换行） */
    private String answer;

    /** 知识分类：报到流程/军训/宿舍/缴费/奖学金/社团/校园生活/其他 */
    private String category;

    /** 关键词（逗号分隔），用于提升TF-IDF匹配精度 */
    private String keywords;

    /** 同义词/相似问法（逗号分隔），用于扩展用户问题的语义覆盖范围 */
    private String synonyms;

    /** 优先级（0-10），数值越大在匹配时越优先推荐 */
    private Integer priority;

    /** 被询问次数（用于热门问题排行） */
    private Integer viewCount;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

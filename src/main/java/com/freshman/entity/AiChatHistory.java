package com.freshman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话历史实体类
 * 功能：记录用户与AI问答机器人的每次对话，包括问题、回答、匹配置信度等，
 *       为后续知识库优化和热门问题统计提供数据支持
 *
 * 所属模块：AI 智能问答模块
 * @author AI Module Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_chat_history")
public class AiChatHistory {

    /** 对话记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（NULL表示匿名访客） */
    private Long userId;

    /** 会话标识（UUID），用于关联同一会话的多轮对话 */
    private String sessionId;

    /** 用户原始问题文本 */
    private String question;

    /** 系统回答内容 */
    private String answer;

    /** 匹配到的知识库条目ID（NULL表示未匹配到，即未知问题） */
    private Long sourceKnowledgeId;

    /** 匹配置信度（0-1之间的浮点数，值越大表示匹配越可靠） */
    private Double confidence;

    /** 是否为未知问题：0-已成功匹配 1-未匹配到知识库 */
    private Integer isUnknown;

    /** 用户IP地址 */
    private String ipAddress;

    /** 对话发生时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.AiChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI 对话历史 Mapper 接口
 * 功能：提供对话历史记录的数据库访问操作
 *
 * 所属模块：AI 智能问答模块
 * @author AI Module Team
 * @version 1.0
 */
@Mapper
public interface AiChatHistoryMapper extends BaseMapper<AiChatHistory> {

    /**
     * 查询指定用户的对话历史（最近50条）
     *
     * @param userId 用户ID
     * @return 对话历史列表（按时间降序）
     */
    @Select("SELECT * FROM ai_chat_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 50")
    List<AiChatHistory> selectByUserId(Long userId);

    /**
     * 查询指定会话的对话历史
     *
     * @param sessionId 会话标识
     * @return 该会话的对话历史列表（按时间升序）
     */
    @Select("SELECT * FROM ai_chat_history WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<AiChatHistory> selectBySessionId(String sessionId);

    /**
     * 统计未知问题数量（供管理员查看，以便补充知识库）
     *
     * @return 未知问题总数
     */
    @Select("SELECT COUNT(*) FROM ai_chat_history WHERE is_unknown = 1")
    long countUnknownQuestions();

    /**
     * 获取最近的未知问题列表（供管理员分析，补充知识库）
     *
     * @param limit 返回条数
     * @return 未知问题列表
     */
    @Select("SELECT * FROM ai_chat_history WHERE is_unknown = 1 ORDER BY create_time DESC LIMIT #{limit}")
    List<AiChatHistory> selectRecentUnknown(int limit);
}

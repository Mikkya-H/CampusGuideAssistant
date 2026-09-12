package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.AiKnowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * AI 知识库 Mapper 接口
 * 功能：提供知识库数据的数据库访问操作，继承MyBatis-Plus的BaseMapper获得通用CRUD能力
 *
 * 所属模块：AI 智能问答模块
 * @author AI Module Team
 * @version 1.0
 */
@Mapper
public interface AiKnowledgeMapper extends BaseMapper<AiKnowledge> {

    /**
     * 查询所有启用的知识条目（按优先级降序排列）
     * AI引擎启动时批量加载到内存中构建向量索引
     *
     * @return 启用的知识条目列表
     */
    @Select("SELECT * FROM ai_knowledge WHERE status = 1 ORDER BY priority DESC, id ASC")
    List<AiKnowledge> selectAllEnabled();

    /**
     * 按分类查询启用的知识条目
     *
     * @param category 知识分类
     * @return 该分类下的知识条目列表
     */
    @Select("SELECT * FROM ai_knowledge WHERE status = 1 AND category = #{category} ORDER BY priority DESC")
    List<AiKnowledge> selectByCategory(String category);

    /**
     * 增加知识条目的被询问次数（用于热门问题统计）
     *
     * @param id 知识条目ID
     */
    @Update("UPDATE ai_knowledge SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    /**
     * 获取热门问题Top N（按被询问次数降序）
     *
     * @param limit 返回条数
     * @return 热门知识条目列表
     */
    @Select("SELECT * FROM ai_knowledge WHERE status = 1 ORDER BY view_count DESC, priority DESC LIMIT #{limit}")
    List<AiKnowledge> selectHotQuestions(int limit);

    /**
     * 搜索知识库（用于管理员后台管理）
     *
     * @param keyword 搜索关键词
     * @return 匹配的知识条目列表
     */
    @Select("SELECT * FROM ai_knowledge WHERE status = 1 AND " +
            "(question LIKE CONCAT('%',#{keyword},'%') OR answer LIKE CONCAT('%',#{keyword},'%') " +
            "OR keywords LIKE CONCAT('%',#{keyword},'%') OR category LIKE CONCAT('%',#{keyword},'%')) " +
            "ORDER BY priority DESC")
    List<AiKnowledge> search(String keyword);
}

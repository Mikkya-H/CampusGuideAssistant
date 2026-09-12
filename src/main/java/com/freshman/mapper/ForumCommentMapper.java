package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.ForumComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 论坛评论数据访问层接口
 * 功能：提供对 forum_comment 表的CRUD操作
 * 负责成员：S
 * 所属模块：交流社区 / 论坛模块
 *
 * @author S
 * @version 1.0
 */
@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {

    /**
     * 级联删除：删除指定帖子下的所有评论（帖子被删除时调用）
     * @param postId 帖子ID
     * @return 删除的评论数量
     */
    @Delete("DELETE FROM forum_comment WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}

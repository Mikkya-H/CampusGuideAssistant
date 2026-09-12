package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛帖子数据访问层接口
 * 功能：提供对 forum_post 表的CRUD操作
 * 负责成员：S
 * 所属模块：交流社区 / 论坛模块
 *
 * @author S
 * @version 1.0
 */
@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

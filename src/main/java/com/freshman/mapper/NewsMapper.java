package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.News;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻公告数据访问层接口
 * 功能：提供对 sys_news 表的CRUD操作
 * 负责成员：Z
 * 所属模块：新闻公告模块
 *
 * @author Z
 * @version 1.0
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

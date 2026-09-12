package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Faq;
import org.apache.ibatis.annotations.Mapper;

/**
 * 常见问题(FAQ)数据访问层接口
 * 功能：提供对 guide_faq 表的CRUD操作
 * 负责成员：W
 * 所属模块：迎新指南 / 常见问题模块
 *
 * @author W
 * @version 1.0
 */
@Mapper
public interface FaqMapper extends BaseMapper<Faq> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

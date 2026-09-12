package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Major;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业信息数据访问层接口
 * 功能：提供对 guide_major 表的CRUD操作
 * 负责成员：W
 * 所属模块：迎新指南 / 专业介绍模块
 *
 * @author W
 * @version 1.0
 */
@Mapper
public interface MajorMapper extends BaseMapper<Major> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

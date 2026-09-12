package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Cafeteria;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食堂信息数据访问层接口
 * 功能：提供对 life_cafeteria 表的CRUD操作
 * 负责成员：S
 * 所属模块：校园生活 / 食堂信息模块
 *
 * @author S
 * @version 1.0
 */
@Mapper
public interface CafeteriaMapper extends BaseMapper<Cafeteria> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

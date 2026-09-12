package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 校园活动数据访问层接口
 * 功能：提供对 life_activity 表的CRUD操作
 * 负责成员：S
 * 所属模块：校园生活 / 校园活动模块
 *
 * @author S
 * @version 1.0
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

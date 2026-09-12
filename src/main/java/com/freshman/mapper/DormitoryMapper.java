package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Dormitory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宿舍信息数据访问层接口
 * 功能：提供对 life_dormitory 表的CRUD操作
 * 负责成员：S
 * 所属模块：校园生活 / 宿舍信息模块
 *
 * @author S
 * @version 1.0
 */
@Mapper
public interface DormitoryMapper extends BaseMapper<Dormitory> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

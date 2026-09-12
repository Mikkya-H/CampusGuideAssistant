package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Building;
import org.apache.ibatis.annotations.Mapper;

/**
 * 校园建筑数据访问层接口
 * 功能：提供对 campus_building 表的CRUD操作
 * 负责成员：Z
 * 所属模块：校园导览模块
 *
 * @author Z
 * @version 1.0
 */
@Mapper
public interface BuildingMapper extends BaseMapper<Building> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

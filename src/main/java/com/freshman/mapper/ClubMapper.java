package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Club;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团信息数据访问层接口
 * 功能：提供对 life_club 表的CRUD操作
 * 负责成员：S
 * 所属模块：校园生活 / 社团信息模块
 *
 * @author S
 * @version 1.0
 */
@Mapper
public interface ClubMapper extends BaseMapper<Club> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

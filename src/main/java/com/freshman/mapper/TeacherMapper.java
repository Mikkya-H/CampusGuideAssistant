package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师信息数据访问层接口
 * 功能：提供对 guide_teacher 表的CRUD操作
 * 负责成员：W
 * 所属模块：迎新指南 / 教师名录模块
 *
 * @author W
 * @version 1.0
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

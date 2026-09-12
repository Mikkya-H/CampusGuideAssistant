package com.freshman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freshman.entity.*;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报到流程数据访问层接口
 * 功能：提供对 guide_registration_step 表的CRUD操作
 * 负责成员：W
 * 所属模块：迎新指南 / 报到流程模块
 *
 * @author W
 * @version 1.0
 */
@Mapper
public interface RegistrationStepMapper extends BaseMapper<RegistrationStep> {
    // 基础CRUD方法由MyBatis-Plus BaseMapper自动提供
}

package com.freshman.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * 功能：配置MyBatis-Plus插件的拦截器链（分页插件）以及元数据自动填充处理器（自动填充创建时间和更新时间）
 * 负责成员：D (Team Lead)
 * 所属模块：系统配置 / 数据访问层配置
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器链
     * 功能：注册分页拦截器，使分页查询功能生效
     *
     * @return MybatisPlusInterceptor 拦截器实例，包含MySQL分页内部拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页内部拦截器，指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 元数据自动填充处理器（内部静态类）
     * 功能：在执行插入和更新操作时，自动为标记了相应注解的字段填充时间值
     * 使用方法：在实体类的createTime字段上添加 @TableField(fill = FieldFill.INSERT)
     *          在实体类的updateTime字段上添加 @TableField(fill = FieldFill.INSERT_UPDATE)
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {

        /**
         * 插入数据时自动填充
         * 功能：为createTime和updateTime字段自动填充当前时间
         *
         * @param metaObject MyBatis元对象，用于反射操作实体对象
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            // 如果字段已有值则保留，无值则填充当前时间
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        }

        /**
         * 更新数据时自动填充
         * 功能：为updateTime字段自动填充当前时间
         *
         * @param metaObject MyBatis元对象，用于反射操作实体对象
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            // 如果字段已有值则保留，无值则填充当前时间
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}

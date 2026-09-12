package com.freshman.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置类
 * 功能：配置Spring MVC的静态资源映射和视图控制器，使上传的图片可以通过URL直接访问
 * 负责成员：D (Team Lead)
 * 所属模块：系统配置模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源映射
     * 功能：将 /uploads/** 的URL路径映射到本地文件系统的 uploads 目录，
     * 使得用户上传的图片、文件等可以通过HTTP直接访问
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录下的uploads文件夹路径
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
        // 将本地文件路径转换为URI格式的字符串
        String uploadPath = uploadDir.toUri().toString();

        // 注册资源映射：/uploads/** -> 本地文件系统
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    /**
     * 配置简单视图控制器
     * 功能：为不需要自定义逻辑的页面直接设置视图跳转，简化控制器代码
     *
     * @param registry 视图控制器注册表
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 登录页面无需额外数据，直接跳转
        registry.addViewController("/login").setViewName("login");
        // 注册页面无需额外数据，直接跳转
        registry.addViewController("/register").setViewName("register");
    }
}

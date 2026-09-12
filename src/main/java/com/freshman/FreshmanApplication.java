package com.freshman;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧迎新系统 - 主启动类
 * Smart Freshman Orientation System
 *
 * 功能：Spring Boot应用程序的入口点，负责：
 *       1. 启动Spring Boot内嵌的Web服务器
 *       2. 自动扫描并注册所有组件、控制器、服务等
 *       3. 扫描并注册MyBatis的Mapper接口
 *       4. 启动完成后打印系统信息到控制台
 *
 * 负责成员：D (Team Lead)
 * 所属模块：系统启动
 *
 * @author Team Freshman
 * @version 1.0.0
 */
@SpringBootApplication  // 标记为Spring Boot启动类，启用自动配置和组件扫描
@MapperScan("com.freshman.mapper")  // 扫描Mapper接口所在的包路径
public class FreshmanApplication {

    /**
     * 应用程序主入口方法
     * 功能：启动Spring Boot应用，并在启动成功后输出欢迎信息
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用程序
        SpringApplication.run(FreshmanApplication.class, args);

        // 启动成功后在控制台打印系统信息
        System.out.println("========================================");
        System.out.println("  智慧迎新系统启动成功！");
        System.out.println("  Smart Freshman Orientation System");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}

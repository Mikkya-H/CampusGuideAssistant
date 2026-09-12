package com.freshman.config;

import com.freshman.entity.User;
import com.freshman.mapper.UserMapper;
import com.freshman.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

/**
 * Spring Security 安全配置类
 * 功能：配置系统的认证与授权机制，包括：
 *       1. 密码加密方式（BCrypt）
 *       2. URL访问权限控制（公开页面、需登录页面、管理员页面）
 *       3. 登录/登出流程配置
 *       4. 从数据库加载用户信息进行认证
 * 负责成员：D (Team Lead)
 * 所属模块：系统配置 / 安全模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码编码器Bean
     * 功能：使用BCrypt算法对密码进行加密和验证，不可逆，安全性高
     *
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     * 功能：定义系统的URL访问权限规则、登录登出行为、CSRF防护等安全策略
     *
     * @param http HttpSecurity配置对象
     * @return 构建好的SecurityFilterChain安全过滤器链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ---------- 请求授权配置 ----------
            .authorizeHttpRequests(auth -> auth
                // 静态资源（CSS、JS、图片等） — 允许所有人访问
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/vendor/**").permitAll()
                // H2数据库控制台 — 开发调试用，允许所有人访问
                .requestMatchers("/h2-console/**").permitAll()
                // 公开页面 — 无需登录即可访问
                .requestMatchers("/", "/login", "/register", "/about").permitAll()
                // 校园导览、迎新指南、校园生活模块 — 允许访客浏览
                .requestMatchers("/campus/**", "/guide/**", "/life/**").permitAll()
                // AI 问答 — 需要登录后才能使用（页面和接口均受保护）
                .requestMatchers("/ai-chat", "/api/ai/**").authenticated()
                // DeepSeek 问答 — 需要登录后才能使用（页面和接口均受保护）
                .requestMatchers("/deepseek-chat", "/api/deepseek/**").authenticated()
                // 导航 API — 允许访客调用
                .requestMatchers("/api/**").permitAll()
                // 新闻公告 — 允许访客浏览
                .requestMatchers("/news/**").permitAll()
                // 论坛首页和帖子详情 — 允许访客浏览
                .requestMatchers("/forum", "/forum/post/**").permitAll()
                // 发帖和评论 — 需要登录认证
                .requestMatchers("/forum/new", "/forum/post/*/comment").authenticated()
                // 个人中心 — 需要登录认证
                .requestMatchers("/user/**").authenticated()
                // 后台管理 — 需要ADMIN角色权限
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 其他所有请求 — 需要登录认证
                .anyRequest().authenticated()
            )
            // ---------- 表单登录配置 ----------
            .formLogin(form -> form
                .loginPage("/login")                  // 自定义登录页面URL
                .loginProcessingUrl("/login")         // 登录表单提交的处理URL
                .defaultSuccessUrl("/", true)          // 登录成功后默认跳转到首页
                .failureUrl("/login?error=true")       // 登录失败后重定向到登录页并显示错误标识
                .permitAll()                           // 登录页面允许所有人访问
            )
            // ---------- 登出配置 ----------
            .logout(logout -> logout
                .logoutUrl("/logout")                 // 登出处理URL
                .logoutSuccessUrl("/login?logout")     // 登出成功后跳转到登录页
                .permitAll()                           // 登出操作允许所有人
            )
            // ---------- 其他安全配置 ----------
            .csrf(csrf -> csrf.disable())              // 禁用CSRF防护（开发阶段，非前后端分离项目可禁用）
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));  // 允许同源iframe，用于H2控制台

        return http.build();
    }

    /**
     * 用户详情服务Bean
     * 功能：从数据库加载用户信息，供Spring Security进行认证使用
     *       优先检查内置管理员账号，再查询数据库中的普通用户
     *
     * @param userService 用户服务，用于从数据库查询用户
     * @return UserDetailsService实例，Spring Security通过它获取用户认证信息
     */
    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return username -> {
            // 内置管理员账号（保留一个默认管理员，防止数据库中管理员被误删后无法登录后台）
            if ("admin".equals(username)) {
                return new org.springframework.security.core.userdetails.User(
                    "admin",
                    passwordEncoder().encode("admin123"),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }
            // 从数据库查询普通用户
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在: " + username);
            }
            // 检查用户是否已被禁用
            if (user.getStatus() == 0) {
                throw new RuntimeException("用户已被禁用");
            }
            // 默认赋予学生角色权限
            String role = "ROLE_STUDENT";
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
            );
        };
    }
}

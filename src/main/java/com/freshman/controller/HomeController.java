package com.freshman.controller;

import com.freshman.entity.News;
import com.freshman.entity.User;
import com.freshman.mapper.NewsMapper;
import com.freshman.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 首页与用户认证控制器
 * 功能：处理系统首页展示、用户登录、用户注册等核心入口功能
 * 负责成员：D (Team Lead)
 * 所属模块：系统入口 / 用户认证模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Controller
public class HomeController {

    private final UserService userService;
    private final NewsMapper newsMapper;

    /**
     * 构造器注入依赖
     * @param userService 用户服务
     * @param newsMapper 新闻数据访问层
     */
    public HomeController(UserService userService, NewsMapper newsMapper) {
        this.userService = userService;
        this.newsMapper = newsMapper;
    }

    /**
     * 系统首页
     * 功能：加载最新3条已发布的新闻在首页展示，优先显示置顶新闻
     *
     * @param model Spring MVC的Model对象，用于向视图传递数据
     * @return 首页视图模板名称
     */
    @GetMapping("/")
    public String index(Model model) {
        // 设置页面标题
        model.addAttribute("title", "智慧迎新系统");

        // 构建查询条件：查询状态为已发布(1)的新闻
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, 1)            // 只查询已发布的新闻
               .orderByDesc(News::getIsTop)        // 置顶新闻优先显示
               .orderByDesc(News::getPublishTime)  // 再按发布时间降序
               .last("LIMIT 3");                   // 最多显示3条
        List<News> newsList = newsMapper.selectList(wrapper);
        model.addAttribute("newsList", newsList);
        return "index";
    }

    /**
     * 登录页面
     * 功能：返回用户登录页面视图
     *
     * @return 登录页面模板名称
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页面
     * 功能：返回用户注册页面视图
     *
     * @return 注册页面模板名称
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 处理用户注册请求
     * 功能：验证用户名唯一性，执行注册操作，并返回相应的提示信息
     *
     * @param user 前端表单提交的用户信息
     * @param ra   RedirectAttributes用于重定向时传递Flash消息
     * @return 成功重定向到登录页，失败重定向回注册页
     */
    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, RedirectAttributes ra) {
        // 检查用户名是否已被注册
        if (userService.findByUsername(user.getUsername()) != null) {
            ra.addFlashAttribute("error", "用户名已存在");
            return "redirect:/register";
        }
        // 执行注册操作
        if (userService.register(user)) {
            ra.addFlashAttribute("success", "注册成功，请登录");
            return "redirect:/login";
        }
        // 注册失败
        ra.addFlashAttribute("error", "注册失败");
        return "redirect:/register";
    }

    /**
     * 关于页面
     * 功能：展示系统或学校的基本介绍信息
     *
     * @return 关于页面模板名称
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}

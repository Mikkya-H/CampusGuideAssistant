package com.freshman.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.freshman.entity.News;
import com.freshman.mapper.NewsMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻公告控制器
 * 功能：负责新闻公告的列表展示和详情查看，支持按分类筛选，并自动统计浏览量
 * 负责成员：Z
 * 所属模块：新闻公告模块
 *
 * @author Z
 * @version 1.0
 */
@Controller
@RequestMapping("/news")
public class NewsController {

    private final NewsMapper newsMapper;

    /**
     * 构造器注入NewsMapper
     * @param newsMapper 新闻数据访问层
     */
    public NewsController(NewsMapper newsMapper) {
        this.newsMapper = newsMapper;
    }

    /**
     * 新闻列表页
     * 功能：展示所有已发布的新闻公告，支持按分类筛选，置顶新闻优先显示
     *
     * @param model    Spring MVC的Model对象
     * @param category 可选分类参数，用于按类别筛选新闻
     * @return 新闻列表视图
     */
    @GetMapping
    public String index(Model model, @RequestParam(required = false) String category) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(News::getStatus, 1);  // 只查询已发布的新闻
            if (category != null && !category.isEmpty()) {
                wrapper.eq(News::getCategory, category);  // 按分类筛选
            }
            // 置顶新闻优先，再按创建时间降序
            wrapper.orderByDesc(News::getIsTop)
                   .orderByDesc(News::getCreateTime);
            List<News> newsList = newsMapper.selectList(wrapper);

            // 防止null值导致前端报错
            if (newsList == null) {
                newsList = new ArrayList<>();
            }
            model.addAttribute("newsList", newsList);
            model.addAttribute("currentCategory", category);
        } catch (Exception e) {
            // 异常处理：查询失败时返回空列表并显示错误信息
            model.addAttribute("newsList", new ArrayList<>());
            model.addAttribute("errorMsg", e.getMessage());
        }
        return "news/index";
    }

    /**
     * 新闻详情页
     * 功能：展示新闻的完整内容，并自动增加该新闻的浏览量
     *
     * @param id    新闻ID（路径变量）
     * @param model Spring MVC的Model对象
     * @return 新闻详情视图，新闻不存在时重定向回列表页
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            News news = newsMapper.selectById(id);
            if (news == null) {
                return "redirect:/news";
            }
            // 浏览量+1，每次查看详情都增加计数
            news.setViewCount(news.getViewCount() + 1);
            newsMapper.updateById(news);
            model.addAttribute("news", news);
        } catch (Exception e) {
            // 异常时重定向回新闻列表页
            return "redirect:/news";
        }
        return "news/detail";
    }
}

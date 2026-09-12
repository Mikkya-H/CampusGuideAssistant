package com.freshman.controller;

import com.freshman.entity.*;
import com.freshman.mapper.*;
import com.freshman.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 交流社区控制器
 * 功能：负责论坛帖子的浏览、发布、详情查看以及评论功能，构建新生交流互动的社区平台
 * 负责成员：S
 * 所属模块：交流社区模块
 *
 * @author S
 * @version 1.0
 */
@Controller
@RequestMapping("/forum")
public class ForumController {

    private final ForumPostMapper postMapper;
    private final ForumCommentMapper commentMapper;
    private final UserService userService;

    /**
     * 构造器注入依赖
     */
    public ForumController(ForumPostMapper postMapper, ForumCommentMapper commentMapper,
                          UserService userService) {
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.userService = userService;
    }

    /**
     * 论坛首页
     * 功能：分页展示论坛帖子列表，支持按分类筛选，置顶帖优先显示，并关联显示发帖人用户名
     *
     * @param model    Spring MVC的Model对象
     * @param category 可选分类参数，用于按帖子分类筛选
     * @return 论坛首页视图
     */
    @GetMapping
    public String index(Model model, @RequestParam(required = false) String category) {
        // 构建查询条件
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ForumPost>();
        wrapper.eq(ForumPost::getStatus, 1);  // 只查询状态正常的帖子
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ForumPost::getCategory, category);  // 按分类筛选
        }
        // 置顶帖优先，然后按创建时间降序
        wrapper.orderByDesc(ForumPost::getIsTop)
               .orderByDesc(ForumPost::getCreateTime);
        List<ForumPost> posts = postMapper.selectList(wrapper);

        // 关联用户名：为每个帖子设置发帖人的显示名称
        for (ForumPost post : posts) {
            User user = userService.getById(post.getUserId());
            if (user != null) {
                post.setUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
        }
        model.addAttribute("posts", posts);
        model.addAttribute("currentCategory", category);
        return "forum/index";
    }

    /**
     * 帖子详情页
     * 功能：展示帖子的完整内容和所有评论，同时增加帖子浏览量
     *
     * @param id    帖子ID（路径变量）
     * @param model Spring MVC的Model对象
     * @return 帖子详情视图，帖子不存在则重定向到论坛首页
     */
    @GetMapping("/post/{id}")
    public String postDetail(@PathVariable Long id, Model model, Principal principal) {
        ForumPost post = postMapper.selectById(id);
        if (post == null) return "redirect:/forum";

        // 浏览量+1
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);

        // 获取作者信息
        User author = userService.getById(post.getUserId());
        if (author != null) {
            post.setUsername(author.getRealName() != null ? author.getRealName() : author.getUsername());
        }

        // 获取当前登录用户信息（用于判断删除权限）
        User currentUser = null;
        if (principal != null) {
            currentUser = userService.findByUsername(principal.getName());
        }
        model.addAttribute("currentUserId", currentUser != null ? currentUser.getId() : null);
        model.addAttribute("isAdmin", currentUser != null && "admin".equals(currentUser.getUsername()));

        // 获取该帖子下的所有评论
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ForumComment>();
        wrapper.eq(ForumComment::getPostId, id)          // 属于当前帖子
               .eq(ForumComment::getStatus, 1)            // 状态正常
               .orderByAsc(ForumComment::getCreateTime); // 按时间升序，最早的评论在前
        List<ForumComment> comments = commentMapper.selectList(wrapper);

        // 关联评论人用户名 + 被回复人用户名
        for (ForumComment comment : comments) {
            User u = userService.getById(comment.getUserId());
            if (u != null) {
                comment.setUsername(u.getRealName() != null ? u.getRealName() : u.getUsername());
            }
            // 如果是回复别人的评论，加载被回复者的用户名
            if (comment.getParentId() != null) {
                ForumComment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null) {
                    User pu = userService.getById(parentComment.getUserId());
                    if (pu != null) {
                        comment.setParentUsername(pu.getRealName() != null ? pu.getRealName() : pu.getUsername());
                    }
                }
            }
        }
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        return "forum/detail";
    }

    /**
     * 发布帖子页面
     * 功能：返回发帖表单页面，需要用户已登录
     *
     * @return 发帖页面视图
     */
    @GetMapping("/new")
    public String newPost() {
        return "forum/new";
    }

    /**
     * 处理发布帖子请求
     * 功能：接收表单提交的帖子内容，关联当前登录用户并保存到数据库
     *
     * @param post      前端提交的帖子实体
     * @param principal Spring Security的认证信息，用于获取当前登录用户的用户名
     * @return 发布成功后重定向到论坛首页
     */
    @PostMapping("/new")
    public String doNewPost(@ModelAttribute ForumPost post, Principal principal,
                            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        if (user != null) {
            // 设置帖子的发布者ID和初始状态
            post.setUserId(user.getId());
            post.setStatus(1);  // 默认状态为正常
            postMapper.insert(post);
            redirectAttributes.addFlashAttribute("success", "帖子发布成功！");
        }
        return "redirect:/forum";
    }

    /**
     * 发表评论/回复
     * 功能：在指定帖子下发表评论，支持回复他人的评论（parentId不为空表示回复）
     */
    @PostMapping("/post/{id}/comment")
    public String addComment(@PathVariable Long id, @RequestParam String content,
                             @RequestParam(required = false) Long parentId,
                             Principal principal, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        // 用户已登录且评论内容非空
        if (user != null && content != null && !content.trim().isEmpty()) {
            ForumComment comment = new ForumComment();
            comment.setPostId(id);
            comment.setUserId(user.getId());
            comment.setContent(content.trim());
            comment.setStatus(1);  // 默认状态为正常
            if (parentId != null) {
                comment.setParentId(parentId);  // 设置被回复的评论ID
            }
            commentMapper.insert(comment);

            // 更新帖子的回复数+1
            ForumPost post = postMapper.selectById(id);
            if (post != null) {
                post.setReplyCount(post.getReplyCount() + 1);
                postMapper.updateById(post);
            }
            redirectAttributes.addFlashAttribute("success", "回复发表成功！");
        }
        return "redirect:/forum/post/" + id;
    }

    /**
     * 删除帖子（级联删除所有评论）
     * 权限控制：帖子作者可删除自己的帖子，管理员可删除任意帖子
     *
     * @param id              帖子ID
     * @param principal       当前登录用户
     * @param redirectAttributes 重定向消息
     * @return 重定向到论坛首页
     */
    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id, Principal principal,
                             RedirectAttributes redirectAttributes) {
        // 未登录用户不允许删除
        if (principal == null) {
            return "redirect:/login";
        }

        ForumPost post = postMapper.selectById(id);
        if (post == null) {
            redirectAttributes.addFlashAttribute("error", "帖子不存在或已被删除");
            return "redirect:/forum";
        }

        User currentUser = userService.findByUsername(principal.getName());
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "用户信息异常");
            return "redirect:/forum";
        }

        // 权限校验：只有帖子作者或管理员可以删除
        boolean isAuthor = currentUser.getId().equals(post.getUserId());
        boolean isAdmin = hasAdminRole(currentUser);

        if (!isAuthor && !isAdmin) {
            redirectAttributes.addFlashAttribute("error", "你没有权限删除该帖子");
            return "redirect:/forum/post/" + id;
        }

        // 第一步：级联删除该帖子下的所有评论
        int deletedComments = commentMapper.deleteByPostId(id);

        // 第二步：删除帖子本身
        postMapper.deleteById(id);

        redirectAttributes.addFlashAttribute("success",
                "帖子已删除（含 " + deletedComments + " 条评论）");
        return "redirect:/forum";
    }

    /**
     * 检查用户是否为管理员角色
     */
    private boolean hasAdminRole(User user) {
        if (user == null) return false;
        // 通过用户名判断是否为管理员（admin用户拥有ROLE_ADMIN角色）
        return "admin".equals(user.getUsername());
    }
}

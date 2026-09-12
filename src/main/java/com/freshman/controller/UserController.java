package com.freshman.controller;

import com.freshman.entity.User;
import com.freshman.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * 用户个人中心控制器
 * 功能：负责用户个人信息的查看和修改，包括基本资料更新和密码修改
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 用户模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造器注入UserService和PasswordEncoder
     * @param userService      用户服务
     * @param passwordEncoder  BCrypt密码编码器，用于密码验证和加密
     */
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 个人中心页面
     * 功能：展示当前登录用户的个人信息
     *
     * @param model     Spring MVC的Model对象
     * @param principal 当前登录用户的认证信息
     * @return 个人中心视图，未登录则重定向到登录页
     */
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        // 未登录用户重定向到登录页
        if (principal == null) return "redirect:/login";
        // 根据用户名查询用户完整信息
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "user/profile";
    }

    /**
     * 更新个人信息
     * 功能：接收表单提交的个人资料并更新到数据库
     *
     * @param formUser  前端表单提交的用户信息（仅包含可修改字段）
     * @param principal 当前登录用户的认证信息
     * @param ra        RedirectAttributes用于传递Flash消息
     * @return 重定向到个人中心页面
     */
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User formUser, Principal principal,
                                RedirectAttributes ra) {
        User user = userService.findByUsername(principal.getName());
        if (user != null) {
            // 只更新允许用户自行修改的字段，防止篡改敏感信息
            user.setRealName(formUser.getRealName());
            user.setPhone(formUser.getPhone());
            user.setEmail(formUser.getEmail());
            user.setCollege(formUser.getCollege());
            user.setMajorName(formUser.getMajorName());
            userService.updateById(user);
            ra.addFlashAttribute("success", "个人信息更新成功");
        }
        return "redirect:/user/profile";
    }

    /**
     * 修改密码
     * 功能：验证原密码正确后，用BCrypt加密新密码并更新
     *
     * @param oldPassword 原密码（用于身份验证）
     * @param newPassword 新密码（将被BCrypt加密后存储）
     * @param principal   当前登录用户的认证信息
     * @param ra          RedirectAttributes用于传递Flash消息
     * @return 重定向到个人中心页面
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Principal principal, RedirectAttributes ra) {
        User user = userService.findByUsername(principal.getName());
        // 验证原密码是否正确
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "原密码错误");
            return "redirect:/user/profile";
        }
        // 加密新密码并保存
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        ra.addFlashAttribute("success", "密码修改成功");
        return "redirect:/user/profile";
    }
}

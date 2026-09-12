package com.freshman.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;

/**
 * 图片上传控制器
 * 功能：提供后台管理中的图片上传功能，支持文件类型过滤、大小限制、按日期分目录存储
 * 负责成员：D (Team Lead)
 * 所属模块：系统管理 / 文件上传模块
 *
 * @author D (Team Lead)
 * @version 1.0
 */
@Controller
@RequestMapping("/admin/upload")
public class UploadController {

    /** 文件上传根目录，从配置文件中读取，默认值为 ./uploads */
    @Value("${app.upload-path:./uploads}")
    private String uploadPath;

    /**
     * 上传图片页面
     * 功能：返回文件上传的表单页面
     *
     * @return 上传页面视图
     */
    @GetMapping
    public String uploadPage() {
        return "admin/upload";
    }

    /**
     * 处理图片上传请求
     * 功能：接收上传的图片文件，进行安全校验后存储到服务器指定目录
     *       校验项目包括：
     *       1. 文件是否为空
     *       2. 文件类型是否允许（仅限图片格式）
     *       3. 文件大小是否超过限制（最大5MB）
     *
     * @param file 上传的图片文件（MultipartFile类型）
     * @param type 文件分类类型（如common、news、avatar等），默认值为"common"
     * @param ra   RedirectAttributes用于传递上传结果消息和图片URL
     * @return 重定向到上传页面，并携带成功/失败提示和图片URL
     */
    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "type", defaultValue = "common") String type,
                         RedirectAttributes ra) {
        // ---------- 1. 非空校验 ----------
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "请选择要上传的图片");
            return "redirect:/admin/upload";
        }

        // ---------- 2. 文件类型校验 ----------
        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName).toLowerCase();  // 获取文件扩展名并转为小写
        if (!ext.matches("jpg|jpeg|png|gif|webp|bmp")) {
            ra.addFlashAttribute("error", "不支持的图片格式，仅支持 jpg/jpeg/png/gif/webp/bmp");
            return "redirect:/admin/upload";
        }

        // ---------- 3. 文件大小校验（限制5MB） ----------
        if (file.getSize() > 5 * 1024 * 1024) {
            ra.addFlashAttribute("error", "图片大小不能超过5MB");
            return "redirect:/admin/upload";
        }

        try {
            // ---------- 4. 构建存储目录 ----------
            // 按类型和日期分目录存储，如：uploads/common/2024/09/01/
            String subDir = type + "/" + java.time.LocalDate.now().toString().replace("-", "/");
            File dir = new File(uploadPath, subDir);
            if (!dir.exists()) dir.mkdirs();  // 目录不存在则递归创建

            // ---------- 5. 生成唯一文件名 ----------
            // 使用UUID防止文件名冲突
            String newName = IdUtil.fastSimpleUUID() + "." + ext;
            File dest = new File(dir, newName);

            // ---------- 6. 保存文件到磁盘 ----------
            file.transferTo(dest);

            // ---------- 7. 生成访问URL并返回 ----------
            // 构建相对于Web服务器的访问路径
            String imageUrl = "/uploads/" + subDir + "/" + newName;
            ra.addFlashAttribute("success", "上传成功！");
            ra.addFlashAttribute("imageUrl", imageUrl);  // 将图片URL传递给前端展示

        } catch (IOException e) {
            ra.addFlashAttribute("error", "上传失败：" + e.getMessage());
        }

        return "redirect:/admin/upload";
    }
}

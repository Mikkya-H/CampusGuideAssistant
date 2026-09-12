package com.freshman.controller;

import com.freshman.common.Result;
import com.freshman.service.AiQaService;
import com.freshman.service.AiQaService.ChatRequest;
import com.freshman.service.AiQaService.ChatResponse;
import com.freshman.service.DeepSeekChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DeepSeek 智能问答控制器
 * 功能：提供 DeepSeek 问答的页面视图和 REST API
 *
 * 页面视图：
 * - GET  /deepseek-chat        → DeepSeek 聊天页面
 *
 * REST API：
 * - POST /api/deepseek/chat    → 发送问题，获取 DeepSeek 回答（核心接口）
 * - GET  /api/deepseek/status  → 查询 DeepSeek 配置状态（是否已配置apiKey）
 *
 * 所属模块：AI 智能问答模块 / DeepSeek 问答
 * @author DeepSeek Module
 * @version 1.0
 */
@Controller
public class DeepSeekController {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekController.class);

    private final DeepSeekChatService deepSeekChatService;
    private final AiQaService aiQaService;

    public DeepSeekController(DeepSeekChatService deepSeekChatService, AiQaService aiQaService) {
        this.deepSeekChatService = deepSeekChatService;
        this.aiQaService = aiQaService;
    }

    // ==================== 页面视图 ====================

    /**
     * DeepSeek 问答聊天页面
     */
    @GetMapping("/deepseek-chat")
    public String chatPage(Model model) {
        model.addAttribute("title", "DeepSeek 问答");
        model.addAttribute("quickQuestions", aiQaService.getQuickQuestions());
        model.addAttribute("hotQuestions", aiQaService.getHotQuestions(10));
        model.addAttribute("categories", aiQaService.getCategories());
        model.addAttribute("configured", deepSeekChatService.isConfigured());
        return "deepseek-chat";
    }

    // ==================== REST API 接口 ====================

    /**
     * DeepSeek 问答核心接口
     *
     * 请求示例：
     * POST /api/deepseek/chat
     * Content-Type: application/json
     * {"question": "宿舍有空调吗？", "sessionId": "abc123"}
     */
    @PostMapping("/api/deepseek/chat")
    @ResponseBody
    public Result<ChatResponse> chat(@RequestBody(required = false) ChatRequest request,
                                      HttpServletRequest httpRequest,
                                      Principal principal) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return Result.error("问题不能为空，请输入您想问的问题");
        }
        if (request.getQuestion().length() > 500) {
            return Result.error("问题过长，请控制在500字以内");
        }

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // 获取客户端IP
        String ipAddress = getClientIp(httpRequest);

        try {
            ChatResponse response = deepSeekChatService.chat(
                    request.getQuestion().trim(), sessionId, ipAddress, null);
            log.info("[DeepSeek API] 问答完成: session={}, isUnknown={}", sessionId, response.getIsUnknown());
            return Result.success(response);
        } catch (Exception e) {
            log.error("[DeepSeek API] 问答异常: {}", e.getMessage(), e);
            return Result.error("DeepSeek 服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 查询 DeepSeek 配置状态（前端用于显示配置提示横幅）
     * GET /api/deepseek/status
     */
    @GetMapping("/api/deepseek/status")
    @ResponseBody
    public Result<Map<String, Object>> status() {
        Map<String, Object> data = new LinkedHashMap<>();
        boolean configured = deepSeekChatService.isConfigured();
        data.put("configured", configured);
        data.put("tip", configured ? "DeepSeek 已就绪" :
                "尚未配置 DeepSeek API Key，请在 application.yml 的 app.ai.deepseek.apiKey 中填入");
        return Result.success(data);
    }

    /**
     * 获取指定会话的历史问答记录（前端进入页面时恢复聊天上下文）
     * GET /api/deepseek/history?sessionId=xxx&limit=50
     */
    @GetMapping("/api/deepseek/history")
    @ResponseBody
    public Result<Map<String, Object>> history(@RequestParam String sessionId,
                                               @RequestParam(defaultValue = "50") int limit) {
        if (limit < 1) limit = 1;
        if (limit > 200) limit = 200;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", sessionId);
        data.put("messages", deepSeekChatService.getHistory(sessionId, limit));
        return Result.success(data);
    }

    // ==================== 辅助方法 ====================

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

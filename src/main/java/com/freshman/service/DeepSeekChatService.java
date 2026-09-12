package com.freshman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.freshman.entity.AiChatHistory;
import com.freshman.mapper.AiChatHistoryMapper;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 智能问答服务
 * 功能：调用 DeepSeek 官方 API（OpenAI 兼容格式）回答新生问题
 *
 * 【架构说明 / 答辩话术】
 * 支持两种模式，由 app.ai.deepseek.useRag 配置项切换：
 *   - useRag=true  → RAG（检索增强生成）模式：先用本地引擎从 ai_knowledge
 *                    知识库检索 Top3 相关材料注入系统提示词，DeepSeek 基于材料
 *                    回答，避免对学校信息"凭空编造"。
 *   - useRag=false → 纯大模型模式（当前默认）：问题直接发给 DeepSeek，
 *                    由模型依据自身通用知识自由回答，不依赖本地知识库。
 * 多轮上下文：每次调用前从 ai_chat_history 按 sessionId 取最近几轮成功问答，
 * 以 messages 数组（user/assistant 交替）形式一并发给模型，实现连续对话。
 * 这与通用 AiQaService 的区别：本服务不内置 Qwen/GLM 等多供应商切换，
 * 专门对接 DeepSeek，作为独立页面（/deepseek-chat）提供服务。
 *
 * 未配置 apiKey 时返回引导提示，不影响应用启动。
 *
 * 所属模块：AI 智能问答模块 / DeepSeek 问答
 * @author DeepSeek Module
 * @version 1.0
 */
@Service
public class DeepSeekChatService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekChatService.class);

    private final AiQaService aiQaService;
    private final AiChatHistoryMapper chatHistoryMapper;

    // ==================== DeepSeek 配置 ====================

    @Value("${app.ai.deepseek.enabled:false}")
    private boolean enabled;

    @Value("${app.ai.deepseek.apiKey:}")
    private String apiKey;

    @Value("${app.ai.deepseek.apiUrl:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${app.ai.deepseek.model:deepseek-v4-flash}")
    private String model;

    /** 是否启用 RAG（检索本地知识库增强回答）；false = 纯大模型自由回答 */
    @Value("${app.ai.deepseek.useRag:false}")
    private boolean useRag;

    public DeepSeekChatService(AiQaService aiQaService, AiChatHistoryMapper chatHistoryMapper) {
        this.aiQaService = aiQaService;
        this.chatHistoryMapper = chatHistoryMapper;
    }

    /** 是否已完成配置（开关打开且 apiKey 非空） */
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * DeepSeek 问答主流程：RAG检索 → 调用DeepSeek API → 保存对话历史
     *
     * @param question  用户问题
     * @param sessionId 会话ID
     * @param ipAddress 客户端IP
     * @param userId    用户ID（匿名时为null）
     * @return 问答响应
     */
    public AiQaService.ChatResponse chat(String question, String sessionId, String ipAddress, Long userId) {
        question = question.trim();

        // ---- 未配置 API Key：返回友好引导，不抛异常 ----
        if (!isConfigured()) {
            AiQaService.ChatResponse resp = new AiQaService.ChatResponse();
            resp.setQuestion(question);
            resp.setAnswer("⚙️ DeepSeek 问答功能尚未配置 API Key。\n\n" +
                    "请按以下步骤完成配置：\n" +
                    "① 访问 https://platform.deepseek.com 注册并登录\n" +
                    "② 左侧菜单「API keys」→「创建 API key」\n" +
                    "③ 复制以 sk- 开头的密钥\n" +
                    "④ 粘贴到 application.yml 的 app.ai.deepseek.apiKey 配置项\n" +
                    "⑤ 重启应用后即可使用\n\n" +
                    "💡 在此之前，你可以使用首页的「AI 智能问答」（哈基油油子），功能同样完整。");
            resp.setConfidence(0.0);
            resp.setCategory("配置提示");
            resp.setIsUnknown(true);
            resp.setRelatedQuestions(aiQaService.getQuickQuestions());
            log.warn("[DeepSeek问答] 未配置 apiKey，返回配置引导");
            return resp;
        }

        // ---- 构建系统提示词：根据 useRag 开关决定是否注入知识库检索材料 ----
        String systemPrompt;
        if (useRag) {
            String context = aiQaService.retrieveContext(question, 3);
            systemPrompt = "你是东北石油大学智慧迎新系统的DeepSeek智能助手，专门为大一新生解答入学相关问题。" +
                    "回答要求：①优先基于参考材料准确回答 ②如果参考材料不足以回答，可以结合常识补充，但不要编造学校具体信息 " +
                    "③友好、简洁、用中文 ④可适当使用emoji。\n\n" +
                    "以下是学校知识库中检索到的参考材料：\n" + (context.isEmpty() ? "（无匹配材料，请谨慎回答）" : context);
            log.info("[DeepSeek问答] RAG模式，已注入知识库检索材料");
        } else {
            systemPrompt = "你是东北石油大学智慧迎新系统的DeepSeek智能助手，专门为大一新生解答入学相关问题。" +
                    "回答要求：①友好、亲切、简洁、用中文 ②可适当使用emoji和排版让回答易读 " +
                    "③结合你的通用知识尽力回答，涉及学校具体信息（如确切日期、费用）时如不确定请如实说明。";
            log.info("[DeepSeek问答] 纯大模型模式（未启用RAG），直接调用API");
        }

        // ---- 构建多轮对话 messages：系统提示词 + 历史问答 + 本轮问题 ----
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        List<AiChatHistory> history = recentSuccessHistory(sessionId, 6);
        for (AiChatHistory h : history) {
            messages.add(Map.of("role", "user", "content", h.getQuestion()));
            messages.add(Map.of("role", "assistant", "content", h.getAnswer()));
        }
        messages.add(Map.of("role", "user", "content", question));
        if (!history.isEmpty()) {
            log.info("[DeepSeek问答] 已携带 {} 轮历史上下文", history.size());
        }

        // ---- 构建 OpenAI 兼容格式请求体（DeepSeek 官方API兼容此格式） ----
        String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":%s,\"temperature\":0.7,\"max_tokens\":800}",
                model,
                JSONUtil.toJsonStr(messages)
        );

        AiQaService.ChatResponse resp = new AiQaService.ChatResponse();
        resp.setQuestion(question);
        resp.setCategory("DeepSeek");

        try {
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            java.io.OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            if (code == 200) {
                String body = readStream(conn.getInputStream());
                String content = extractJsonField(body, "content");
                if (content != null && !content.isEmpty()) {
                    resp.setAnswer(content);
                    resp.setConfidence(0.95);
                    resp.setIsUnknown(false);
                    resp.setRelatedQuestions(aiQaService.getQuickQuestions());
                    log.info("[DeepSeek问答] 回答成功, 长度={}", content.length());
                    saveHistory(userId, sessionId, question, content, ipAddress);
                    return resp;
                }
                log.warn("[DeepSeek问答] 响应中未找到content字段: {}",
                        body.substring(0, Math.min(200, body.length())));
            } else if (code == 401) {
                resp.setAnswer("🔑 DeepSeek API Key 无效或已被删除，请到 https://platform.deepseek.com 检查后更新 application.yml 配置。");
            } else if (code == 402) {
                resp.setAnswer("💰 DeepSeek 账户余额不足，请充值后重试（注册赠送额度用完时会报此错误）。");
            } else if (code == 429) {
                resp.setAnswer("⏳ 请求过于频繁（触发限流），请稍等几秒再试。");
            } else {
                String errBody = conn.getErrorStream() != null ? readStream(conn.getErrorStream()) : "";
                log.warn("[DeepSeek问答] API返回 {}: {}", code, errBody);
                resp.setAnswer(" DeepSeek 服务返回异常（状态码 " + code + "），请稍后再试。");
            }
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[DeepSeek问答] API调用超时");
            resp.setAnswer("⏱️ DeepSeek 响应超时，请稍后重试。");
        } catch (Exception e) {
            log.warn("[DeepSeek问答] API调用异常: {}", e.getMessage());
            resp.setAnswer("🚫 无法连接 DeepSeek 服务，请检查网络后重试。\n\n（" + e.getMessage() + "）");
        }

        resp.setConfidence(0.0);
        resp.setIsUnknown(true);
        resp.setRelatedQuestions(aiQaService.getQuickQuestions());
        saveHistory(userId, sessionId, question, resp.getAnswer(), ipAddress);
        return resp;
    }

    // ==================== 辅助方法 ====================

    /**
     * 查询指定会话最近 N 轮成功的问答记录（按时间正序返回）
     * 仅取 isUnknown=0 的记录，配置引导/服务异常等无效回答不进入模型上下文
     */
    private List<AiChatHistory> recentSuccessHistory(String sessionId, int limit) {
        try {
            List<AiChatHistory> list = chatHistoryMapper.selectList(
                    new LambdaQueryWrapper<AiChatHistory>()
                            .eq(AiChatHistory::getSessionId, sessionId)
                            .eq(AiChatHistory::getIsUnknown, 0)
                            .orderByDesc(AiChatHistory::getId)
                            .last("LIMIT " + limit));
            java.util.Collections.reverse(list);
            return list;
        } catch (Exception e) {
            log.warn("[DeepSeek问答] 查询历史上下文失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 获取指定会话的历史问答（供前端恢复聊天记录用）
     * @return [{question, answer, createTime}]，按时间正序
     */
    public List<Map<String, Object>> getHistory(String sessionId, int limit) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<AiChatHistory> list = chatHistoryMapper.selectList(
                    new LambdaQueryWrapper<AiChatHistory>()
                            .eq(AiChatHistory::getSessionId, sessionId.trim())
                            .eq(AiChatHistory::getIsUnknown, 0)
                            .orderByDesc(AiChatHistory::getId)
                            .last("LIMIT " + limit));
            java.util.Collections.reverse(list);
            for (AiChatHistory h : list) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("question", h.getQuestion());
                item.put("answer", h.getAnswer());
                item.put("createTime", h.getCreateTime());
                result.add(item);
            }
        } catch (Exception e) {
            log.warn("[DeepSeek问答] 获取历史记录失败: {}", e.getMessage());
        }
        return result;
    }

    private void saveHistory(Long userId, String sessionId, String question, String answer, String ipAddress) {
        try {
            AiChatHistory history = new AiChatHistory();
            history.setUserId(userId);
            history.setSessionId(sessionId);
            history.setQuestion(question);
            history.setAnswer(answer);
            history.setConfidence(null);
            history.setIsUnknown(0);
            history.setIpAddress(ipAddress);
            chatHistoryMapper.insert(history);
        } catch (Exception e) {
            log.warn("[DeepSeek问答] 保存历史失败: {}", e.getMessage());
        }
    }

    private String readStream(java.io.InputStream is) throws java.io.IOException {
        java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    /** 转义JSON字符串特殊字符 */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 从JSON字符串提取指定字段值（适配OpenAI兼容格式，无第三方JSON依赖）
     */
    private String extractJsonField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIdx = json.indexOf(key);
        String lastValue = null;
        while (keyIdx != -1) {
            int colonIdx = json.indexOf(":", keyIdx);
            if (colonIdx == -1) break;
            int valueStart = colonIdx + 1;
            while (valueStart < json.length() && json.charAt(valueStart) == ' ') valueStart++;
            if (valueStart >= json.length()) break;

            if (json.charAt(valueStart) == '"') {
                int valueEnd = valueStart + 1;
                while (valueEnd < json.length()) {
                    if (json.charAt(valueEnd) == '"' && json.charAt(valueEnd - 1) != '\\') break;
                    valueEnd++;
                }
                String val = json.substring(valueStart + 1, valueEnd)
                        .replace("\\\"", "\"")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
                        .replace("\\\\", "\\");
                if (!val.isEmpty()) lastValue = val;
            } else {
                int valueEnd = valueStart;
                while (valueEnd < json.length() && ",}]".indexOf(json.charAt(valueEnd)) == -1) valueEnd++;
                String val = json.substring(valueStart, valueEnd).trim();
                if (!val.isEmpty()) lastValue = val;
            }
            keyIdx = json.indexOf(key, valueStart);
        }
        return lastValue;
    }
}

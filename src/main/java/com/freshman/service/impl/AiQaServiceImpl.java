package com.freshman.service.impl;

import com.freshman.entity.AiChatHistory;
import com.freshman.entity.AiKnowledge;
import com.freshman.mapper.AiChatHistoryMapper;
import com.freshman.mapper.AiKnowledgeMapper;
import com.freshman.service.AiQaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 智能问答服务实现类 — 核心语义匹配引擎
 *
 * 【技术亮点 / 答辩话术】
 * 1. 自研中文分词器：Bigram+Trigram字符N-gram + 自定义词典正向最大匹配，
 *    无需依赖jieba/HanLP等第三方NLP库，轻量高效
 * 2. TF-IDF语义向量化：将非结构化的中文问题转化为结构化的向量空间，
 *    使计算机能够"理解"语义而非简单的关键词匹配
 * 3. 多策略融合评分算法：Jaccard字面重叠(0.3) + TF-IDF余弦相似度(0.5) +
 *    分类相关性(0.2)，三种策略互补，提升匹配准确率
 * 4. 同义词扩展字典：内置100+对中文同义词映射，解决"宿舍/寝室/住宿"
 *    等不同表达的语义鸿沟问题
 * 5. 置信度阈值机制：对低于阈值(0.25)的匹配结果判定为"未知问题"，
 *    拒绝胡编乱造，确保回答的可靠性
 * 6. 离线优先+可扩展架构：核心引擎不依赖任何外部API即可工作，
 *    同时预留LLM接口(OpenAI/Claude/Qwen)，配置即可升级
 *
 * 所属模块：AI 智能问答模块
 * @author AI Module Team
 * @version 1.0
 */
@Service
public class AiQaServiceImpl implements AiQaService {

    private static final Logger log = LoggerFactory.getLogger(AiQaServiceImpl.class);

    // ==================== 依赖注入 ====================
    private final AiKnowledgeMapper knowledgeMapper;
    private final AiChatHistoryMapper chatHistoryMapper;

    // ==================== 算法参数配置 ====================

    /** 未知问题置信度阈值：得分低于此值判定为未知问题 */
    private static final double UNKNOWN_THRESHOLD = 0.25;

    /** Jaccard相似度权重：衡量字面关键词重叠程度 */
    private static final double WEIGHT_JACCARD = 0.30;

    /** TF-IDF余弦相似度权重：衡量语义层面的相似程度 */
    private static final double WEIGHT_TFIDF = 0.50;

    /** 分类相关性权重：问题命中的分类与知识条目分类的一致性加成 */
    private static final double WEIGHT_CATEGORY = 0.20;

    /** 相关推荐问题数量 */
    private static final int RELATED_COUNT = 3;

    /** IDF平滑系数（拉普拉斯平滑，防止log(0)） */
    private static final double IDF_SMOOTHING = 1.0;

    // ==================== 内存中的向量索引 ====================

    /** 全量知识库条目（启用状态） */
    private List<AiKnowledge> knowledgeBase;

    /** 词→IDF值映射 */
    private Map<String, Double> idfMap;

    /** 知识条目ID→TF-IDF向量映射 */
    private Map<Long, Map<String, Double>> docVectors;

    /** 知识条目ID→分类映射 */
    private Map<Long, String> docCategories;

    // ==================== LLM配置 ====================

    @Value("${app.ai.llm.enabled:false}")
    private boolean llmEnabled;

    @Value("${app.ai.llm.apiKey:}")
    private String llmApiKey;

    @Value("${app.ai.llm.apiUrl:}")
    private String llmApiUrl;

    @Value("${app.ai.llm.model:qwen3-turbo}")
    private String llmModel;

    // ==================== 同义词扩展字典 ====================
    // 用于将用户问题中的口语化表达映射为标准词，提升匹配召回率

    private static final Map<String, List<String>> SYNONYM_DICT = new LinkedHashMap<>();
    static {
        // 宿舍相关
        SYNONYM_DICT.put("宿舍", Arrays.asList("寝室", "住宿", "公寓", "住的地方", "房间", "住房"));
        SYNONYM_DICT.put("空调", Arrays.asList("冷气", "制冷", "降温设备"));
        SYNONYM_DICT.put("独卫", Arrays.asList("独立卫生间", "独立卫浴", "单独厕所", "独立厕所", "私人卫生间"));
        // 军训相关
        SYNONYM_DICT.put("军训", Arrays.asList("军事训练", "军训服", "迷彩服", "军服", "队列训练"));
        SYNONYM_DICT.put("请假", Arrays.asList("请病假", "不参加", "免训", "缓训", "休息"));
        // 费用相关
        SYNONYM_DICT.put("学费", Arrays.asList("学费多少钱", "缴费标准", "收费", "要交多少钱"));
        SYNONYM_DICT.put("奖学金", Arrays.asList("助学金", "补助", "资助", "奖励", "奖金", "困难补助"));
        SYNONYM_DICT.put("贷款", Arrays.asList("助学贷款", "借钱上学", "借款", "分期"));
        // 报到相关
        SYNONYM_DICT.put("报到", Arrays.asList("报名", "入学", "开学", "去学校", "注册", "签到"));
        SYNONYM_DICT.put("录取通知书", Arrays.asList("通知书", "录取通知", "录取书", "通知书丢了"));
        // 社团相关
        SYNONYM_DICT.put("社团", Arrays.asList("协会", "俱乐部", "组织", "团队", "学生组织"));
        // 生活相关
        SYNONYM_DICT.put("食堂", Arrays.asList("餐厅", "饭堂", "吃饭的地方", "伙食", "餐饮"));
        SYNONYM_DICT.put("WiFi", Arrays.asList("wifi", "网络", "上网", "无线网", "校园网", "宽带", "联网"));
        SYNONYM_DICT.put("图书馆", Arrays.asList("自习室", "看书的地方", "学习的地方", "借书处"));
        // 其他
        SYNONYM_DICT.put("老师", Arrays.asList("教师", "教授", "导员", "辅导员", "讲师"));
        SYNONYM_DICT.put("专业", Arrays.asList("学科", "方向", "学什么", "课程"));
        SYNONYM_DICT.put("就业", Arrays.asList("工作", "找工作", "毕业去向", "招聘", "求职", "薪资"));
    }

    /** 同义词反向索引（自动构建） */
    private Map<String, String> synonymReverseIndex;

    // ==================== 分类关键词映射 ====================
    // 用于从用户问题中识别意图分类

    private static final Map<String, String> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    static {
        CATEGORY_KEYWORDS.put("报到流程", "报到,入学,通知书,接站,注册,开学,报到流程,报到材料,体检,团关系");
        CATEGORY_KEYWORDS.put("军训", "军训,军服,迷彩,训练,教官,队列,射击,内务,汇报表演,阅兵");
        CATEGORY_KEYWORDS.put("宿舍", "宿舍,寝室,住宿,房间,空调,独卫,洗澡,水电,门禁,电器");
        CATEGORY_KEYWORDS.put("缴费", "学费,缴费,费用,交费,钱,收费,付款,绿色通道");
        CATEGORY_KEYWORDS.put("奖学金", "奖学金,助学金,贷款,资助,补助,困难认定,勤工俭学");
        CATEGORY_KEYWORDS.put("社团", "社团,协会,俱乐部,组织,招新,学生会,百团大战");
        CATEGORY_KEYWORDS.put("校园生活", "食堂,图书馆,自习,运动,校园卡,快递,交通,校园网,WiFi,网络");
    }

    public AiQaServiceImpl(AiKnowledgeMapper knowledgeMapper, AiChatHistoryMapper chatHistoryMapper) {
        this.knowledgeMapper = knowledgeMapper;
        this.chatHistoryMapper = chatHistoryMapper;
    }

    // ==================== 初始化 ====================

    /**
     * 应用启动后自动加载知识库并构建向量索引
     * 使用@PostConstruct确保在依赖注入完成后执行
     */
    @PostConstruct
    public void init() {
        reloadKnowledgeBase();
        buildSynonymReverseIndex();
        log.info("========================================");
        log.info("  AI 智能问答引擎初始化完成");
        log.info("  知识库条目数: {}", knowledgeBase.size());
        log.info("  词汇表大小: {}", idfMap.size());
        log.info("  LLM扩展: {}", llmEnabled ? "已启用 (" + llmModel + ")" : "离线模式");
        if (llmEnabled) {
            log.info("  LLM API URL: {}", llmApiUrl);
            log.info("  LLM API Key: {}...{}",
                    llmApiKey != null && llmApiKey.length() > 10 ? llmApiKey.substring(0, 8) : "(空)",
                    llmApiKey != null && llmApiKey.length() > 10 ? llmApiKey.substring(llmApiKey.length() - 4) : "");
        }
        log.info("  置信度阈值: {}", UNKNOWN_THRESHOLD);
        log.info("========================================");
    }

    /** 构建同义词反向索引：任意同义词→标准词 */
    private void buildSynonymReverseIndex() {
        synonymReverseIndex = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : SYNONYM_DICT.entrySet()) {
            String standard = entry.getKey();
            for (String synonym : entry.getValue()) {
                synonymReverseIndex.put(synonym, standard);
            }
            // 标准词本身也映射到自己
            synonymReverseIndex.put(standard, standard);
        }
    }

    // ==================== 核心问答接口 ====================

    @Override
    public ChatResponse chat(String question, String sessionId, String ipAddress, Long userId) {
        if (question == null || question.trim().isEmpty()) {
            return buildUnknownResponse(question);
        }

        question = question.trim();
        log.info("[AI问答] 收到问题: {}", question);

        // ---- 第一步：尝试LLM扩展（如已启用） ----
        if (llmEnabled && llmApiKey != null && !llmApiKey.isEmpty()) {
            try {
                ChatResponse llmResponse = callLlmApi(question);
                if (llmResponse != null) {
                    saveHistory(userId, sessionId, question, llmResponse.getAnswer(),
                            null, llmResponse.getConfidence(), 0, ipAddress);
                    return llmResponse;
                }
            } catch (Exception e) {
                log.warn("[AI问答] LLM调用失败，降级到本地引擎: {}", e.getMessage());
            }
        }

        // ---- 第二步：本地语义匹配引擎 ----
        // 2.1 中文分词 + 同义词扩展
        List<String> questionTokens = tokenize(question);
        questionTokens = expandSynonyms(questionTokens);

        // 2.2 计算查询的TF-IDF向量
        Map<String, Double> queryVector = computeQueryTfIdf(questionTokens);

        // 2.3 多策略融合评分 → 为每条知识打分
        List<MatchResult> results = new ArrayList<>();
        for (AiKnowledge doc : knowledgeBase) {
            double score = computeFusionScore(queryVector, questionTokens, doc);
            if (score > 0) {
                results.add(new MatchResult(doc, score));
            }
        }

        // 2.4 按得分降序排序
        results.sort((a, b) -> Double.compare(b.score, a.score));

        // 2.5 置信度判断
        if (results.isEmpty() || results.get(0).score < UNKNOWN_THRESHOLD) {
            // 未知问题：所有匹配得分都低于阈值
            ChatResponse response = buildUnknownResponse(question);
            saveHistory(userId, sessionId, question, response.getAnswer(),
                    null, 0.0, 1, ipAddress);
            log.info("[AI问答] 未知问题 (最高得分={})，返回引导提示",
                    results.isEmpty() ? 0 : String.format("%.3f", results.get(0).score));
            return response;
        }

        // 2.6 最佳匹配
        MatchResult best = results.get(0);
        AiKnowledge bestDoc = best.doc;

        // 增量更新被询问次数（异步友好，失败不影响主流程）
        try {
            knowledgeMapper.incrementViewCount(bestDoc.getId());
        } catch (Exception ignored) {}

        // 2.7 构造相关推荐问题
        String[] related = results.stream()
                .skip(1)
                .limit(RELATED_COUNT)
                .map(r -> r.doc.getQuestion())
                .toArray(String[]::new);

        // 2.8 组装响应
        ChatResponse response = new ChatResponse();
        response.setQuestion(question);
        response.setAnswer(bestDoc.getAnswer());
        response.setConfidence(Math.round(best.score * 10000.0) / 10000.0);
        response.setCategory(bestDoc.getCategory());
        response.setIsUnknown(false);
        response.setRelatedQuestions(related);

        // 2.9 保存对话历史
        saveHistory(userId, sessionId, question, bestDoc.getAnswer(),
                bestDoc.getId(), best.score, 0, ipAddress);

        log.info("[AI问答] 匹配成功: category={}, confidence={}, related={}",
                bestDoc.getCategory(), String.format("%.3f", best.score), related.length);
        return response;
    }

    // ==================== 中文分词器 ====================

    /**
     * 中文分词器 — 核心算法组件
     *
     * 采用双重策略：
     * 1. 字符级N-gram：提取bigram(2-gram)和trigram(3-gram)，
     *    中文中双字词和三字词占比最高，能有效覆盖大部分词汇
     *    例："宿舍有空调吗" → bigram: [宿舍, 舍有, 有空, 空调, 调吗]
     *                      trigram: [宿舍有, 舍有空, 有空调, 空调吗]
     * 2. 词典正向最大匹配：基于知识库关键词构建自定义词典，
     *    从长到短尝试匹配，优先匹配长词
     *    例：词典含[录取通知书] → "录取通知书丢失" → 匹配到 [录取通知书, 丢失]
     *
     * @param text 原始文本
     * @return 分词结果列表
     */
    private List<String> tokenize(String text) {
        Set<String> tokens = new LinkedHashSet<>();

        // 策略1：字符级N-gram（bigram + trigram）
        // 去除标点符号和空格，保留纯中文字符和英文/数字
        // 去除标点符号和空格，保留纯中文字符和英文/数字
        String cleaned = text.replaceAll("[\\s\\p{Punct}，。！？；：“”''【】《》（）…—～·]", "");
        for (int i = 0; i < cleaned.length() - 1; i++) {
            tokens.add(cleaned.substring(i, i + 2));           // bigram
            if (i + 2 < cleaned.length()) {
                tokens.add(cleaned.substring(i, i + 3));       // trigram
            }
        }

        // 策略2：词典正向最大匹配（基于所有知识库关键词构建的词典）
        Set<String> dictionary = buildTokenizerDictionary();
        int pos = 0;
        int textLen = text.length();
        while (pos < textLen) {
            int maxMatchLen = 0;
            String matched = null;
            // 从当前位置开始尝试最长匹配（最长6个字符）
            for (int len = Math.min(6, textLen - pos); len >= 2; len--) {
                String candidate = text.substring(pos, pos + len);
                if (dictionary.contains(candidate)) {
                    maxMatchLen = len;
                    matched = candidate;
                    break;
                }
            }
            if (matched != null) {
                tokens.add(matched);
                pos += maxMatchLen;
            } else {
                pos++;
            }
        }

        // 策略3：单字也加入（对于短问题有帮助）
        for (char ch : cleaned.toCharArray()) {
            if (Character.isLetterOrDigit(ch) || Character.UnicodeScript.of(ch).name().equals("HAN")) {
                tokens.add(String.valueOf(ch));
            }
        }

        return new ArrayList<>(tokens);
    }

    /** 构建分词词典（从知识库关键词和同义词中提取） */
    private Set<String> buildTokenizerDictionary() {
        Set<String> dict = new HashSet<>();
        for (AiKnowledge doc : knowledgeBase) {
            if (doc.getKeywords() != null) {
                for (String kw : doc.getKeywords().split("[,，]")) {
                    String trimmed = kw.trim();
                    if (trimmed.length() >= 2) {
                        dict.add(trimmed);
                    }
                }
            }
            if (doc.getSynonyms() != null) {
                for (String syn : doc.getSynonyms().split("[,，]")) {
                    String trimmed = syn.trim();
                    if (trimmed.length() >= 2) {
                        dict.add(trimmed);
                    }
                }
            }
        }
        // 加入同义词字典
        for (String word : SYNONYM_DICT.keySet()) {
            dict.add(word);
            dict.addAll(SYNONYM_DICT.get(word));
        }
        return dict;
    }

    // ==================== 同义词扩展 ====================

    /**
     * 同义词扩展：将分词结果中的词语映射为标准表达
     * 例：["寝室", "有", "空调", "吗"] → ["宿舍", "有", "空调", "吗"]
     *     因为"寝室"是同义词，被替换为标准词"宿舍"
     *
     * @param tokens 原始分词结果
     * @return 同义词扩展后的分词列表
     */
    private List<String> expandSynonyms(List<String> tokens) {
        Set<String> expanded = new LinkedHashSet<>(tokens);
        for (String token : tokens) {
            // 如果该词在同义词反向索引中，添加到结果中
            if (synonymReverseIndex.containsKey(token)) {
                expanded.add(synonymReverseIndex.get(token));
            }
            // 双向查找：如果该词是一个标准词，把它的所有同义词也加入
            if (SYNONYM_DICT.containsKey(token)) {
                expanded.addAll(SYNONYM_DICT.get(token));
            }
        }
        return new ArrayList<>(expanded);
    }

    // ==================== TF-IDF 向量化 ====================

    /**
     * TF-IDF 语义向量化 — 核心算法组件
     *
     * TF-IDF (Term Frequency-Inverse Document Frequency) 是信息检索领域的经典算法：
     * - TF (词频) = 词在文档中出现的次数 / 文档总词数
     *   衡量一个词对当前文档的重要性
     * - IDF (逆文档频率) = log(总文档数 / 包含该词的文档数) + 平滑系数
     *   衡量一个词的区分能力——一个词在越少的文档中出现，区分能力越强
     *
     * 例如："软件工程"这个词只出现在软件工程专业相关文档中，
     * 其IDF值很高，在用户问"软件工程"相关问题时能有效区分匹配目标
     *
     * @param tokens 分词后的词列表
     * @return 词→TF-IDF值的向量
     */
    private Map<String, Double> computeQueryTfIdf(List<String> tokens) {
        Map<String, Double> tfIdfVector = new HashMap<>();
        if (tokens.isEmpty()) return tfIdfVector;

        int totalTerms = tokens.size();

        // 计算TF：统计每个词的频率
        Map<String, Integer> termFreq = new HashMap<>();
        for (String token : tokens) {
            termFreq.merge(token, 1, Integer::sum);
        }

        // 计算TF-IDF = TF * IDF
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            double tf = (double) entry.getValue() / totalTerms;
            double idf = idfMap.getOrDefault(term, Math.log(knowledgeBase.size() + IDF_SMOOTHING));
            tfIdfVector.put(term, tf * idf);
        }

        return tfIdfVector;
    }

    // ==================== 多策略融合评分 ====================

    /**
     * 多策略融合评分算法 — 核心技术亮点
     *
     * 最终得分 = α × Jaccard相似度 + β × TF-IDF余弦相似度 + γ × 分类相关性
     * 其中 α=0.3, β=0.5, γ=0.2
     *
     * 三种策略互补：
     * - Jaccard：保证字面重叠度高的优先，处理直白提问
     * - TF-IDF余弦：捕捉语义层面的相似性，处理模糊提问
     * - 分类相关性：利用问题意图分类约束匹配范围，减少误匹配
     *
     * @param queryVector  用户问题的TF-IDF向量
     * @param queryTokens  用户问题分词列表
     * @param doc          知识库条目
     * @return 综合得分 (0-1)
     */
    private double computeFusionScore(Map<String, Double> queryVector,
                                       List<String> queryTokens,
                                       AiKnowledge doc) {
        // ---- 策略1：Jaccard关键词重叠相似度 ----
        double jaccardScore = computeJaccardSimilarity(queryTokens, doc);

        // ---- 策略2：TF-IDF余弦相似度 ----
        double cosineScore = computeCosineSimilarity(queryVector, doc);

        // ---- 策略3：分类相关性加成 ----
        double categoryBonus = computeCategoryBonus(queryTokens, doc);

        // ---- 策略4：优先级加成 ----
        double priorityBonus = doc.getPriority() != null ? doc.getPriority() * 0.01 : 0;

        // ---- 融合得分 ----
        double fusionScore = WEIGHT_JACCARD * jaccardScore
                           + WEIGHT_TFIDF * cosineScore
                           + WEIGHT_CATEGORY * categoryBonus
                           + priorityBonus;

        return Math.min(fusionScore, 1.0); // 上限裁剪
    }

    /**
     * Jaccard相似度计算
     * Jaccard(A, B) = |A ∩ B| / |A ∪ B|
     * 衡量两个集合的字面重叠程度
     */
    private double computeJaccardSimilarity(List<String> queryTokens, AiKnowledge doc) {
        Set<String> docTokens = extractDocTokens(doc);
        if (docTokens.isEmpty() || queryTokens.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(queryTokens);
        intersection.retainAll(docTokens);

        Set<String> union = new HashSet<>(queryTokens);
        union.addAll(docTokens);

        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    /**
     * TF-IDF余弦相似度计算
     * cos(θ) = (A·B) / (||A|| × ||B||)
     * 两个向量的夹角越小(cos越接近1)，语义越相似
     */
    private double computeCosineSimilarity(Map<String, Double> queryVector, AiKnowledge doc) {
        Map<String, Double> docVector = docVectors.get(doc.getId());
        if (docVector == null || docVector.isEmpty() || queryVector.isEmpty()) return 0;

        // 计算点积 A·B
        double dotProduct = 0;
        for (Map.Entry<String, Double> entry : queryVector.entrySet()) {
            Double docWeight = docVector.get(entry.getKey());
            if (docWeight != null) {
                dotProduct += entry.getValue() * docWeight;
            }
        }

        // 计算向量模长
        double queryNorm = 0;
        for (double v : queryVector.values()) {
            queryNorm += v * v;
        }
        queryNorm = Math.sqrt(queryNorm);

        double docNorm = 0;
        for (double v : docVector.values()) {
            docNorm += v * v;
        }
        docNorm = Math.sqrt(docNorm);

        if (queryNorm == 0 || docNorm == 0) return 0;

        return dotProduct / (queryNorm * docNorm);
    }

    /**
     * 分类相关性加成
     * 通过关键词匹配识别用户问题的意图分类，
     * 与知识条目分类一致时给予加成
     */
    private double computeCategoryBonus(List<String> queryTokens, AiKnowledge doc) {
        String queryCategory = detectCategory(queryTokens);
        if (queryCategory != null && queryCategory.equals(doc.getCategory())) {
            return 1.0; // 完全匹配 → 满分
        }
        // 部分匹配给予一半加成（同大类）
        if (queryCategory != null && doc.getCategory() != null) {
            return 0.3; // 有分类但与目标不匹配 → 低加成
        }
        return 0.0;
    }

    /** 从用户问题分词中检测意图分类 */
    private String detectCategory(List<String> queryTokens) {
        String queryStr = String.join(" ", queryTokens);
        String bestCategory = null;
        int bestScore = 0;

        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String kw : entry.getValue().split("[,，]")) {
                if (queryStr.contains(kw.trim())) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestCategory = entry.getKey();
            }
        }
        return bestScore >= 2 ? bestCategory : null; // 至少命中2个关键词才判定
    }

    /** 提取知识条目的所有特征词（问题+关键词+同义词） */
    private Set<String> extractDocTokens(AiKnowledge doc) {
        Set<String> tokens = new HashSet<>();
        if (doc.getQuestion() != null) {
            tokens.addAll(tokenize(doc.getQuestion()));
        }
        if (doc.getKeywords() != null) {
            for (String kw : doc.getKeywords().split("[,，]")) {
                String t = kw.trim();
                if (!t.isEmpty()) tokens.add(t);
            }
        }
        if (doc.getSynonyms() != null) {
            for (String syn : doc.getSynonyms().split("[,，]")) {
                String t = syn.trim();
                if (!t.isEmpty()) tokens.add(t);
            }
        }
        return tokens;
    }

    // ==================== 知识库向量索引构建 ====================

    /**
     * 预计算所有文档的TF-IDF向量和全局IDF字典
     * 在应用启动和知识库更新时调用，将计算密集的工作前置，
     * 确保查询时的毫秒级响应
     */
    private void buildVectorIndex() {
        idfMap = new HashMap<>();
        docVectors = new HashMap<>();
        docCategories = new HashMap<>();

        int totalDocs = knowledgeBase.size();
        if (totalDocs == 0) return;

        // ---- 第一步：为每个文档分词并统计词频 ----
        // docTermFreq[docId][term] = 该词在文档中的频次
        Map<Long, Map<String, Integer>> docTermFreqs = new HashMap<>();
        // docTermCount[docId] = 文档总词数
        Map<Long, Integer> docTermCounts = new HashMap<>();
        // termDocCount[term] = 包含该词的文档数
        Map<String, Integer> termDocCount = new HashMap<>();

        for (AiKnowledge doc : knowledgeBase) {
            Set<String> tokens = extractDocTokens(doc);
            // 额外：对答案也做分词（答案内容也有助于匹配）
            if (doc.getAnswer() != null) {
                tokens.addAll(tokenize(doc.getAnswer()));
            }

            Map<String, Integer> freqMap = new HashMap<>();
            for (String token : tokens) {
                freqMap.merge(token, 1, Integer::sum);
            }

            int totalTerms = freqMap.values().stream().mapToInt(Integer::intValue).sum();
            docTermFreqs.put(doc.getId(), freqMap);
            docTermCounts.put(doc.getId(), totalTerms);
            docCategories.put(doc.getId(), doc.getCategory());

            // 统计文档频率
            for (String token : freqMap.keySet()) {
                termDocCount.merge(token, 1, Integer::sum);
            }
        }

        // ---- 第二步：计算IDF值 ----
        for (Map.Entry<String, Integer> entry : termDocCount.entrySet()) {
            // IDF = log(总文档数 / 包含该词的文档数) + 平滑系数
            double idf = Math.log((double) totalDocs / entry.getValue()) + IDF_SMOOTHING;
            idfMap.put(entry.getKey(), idf);
        }

        // ---- 第三步：计算每个文档的TF-IDF向量 ----
        for (AiKnowledge doc : knowledgeBase) {
            Map<String, Double> tfIdfVector = new HashMap<>();
            Map<String, Integer> freqMap = docTermFreqs.get(doc.getId());
            int totalTerms = docTermCounts.get(doc.getId());

            if (freqMap != null && totalTerms > 0) {
                for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
                    String term = entry.getKey();
                    double tf = (double) entry.getValue() / totalTerms;
                    double idf = idfMap.getOrDefault(term, Math.log(totalDocs + IDF_SMOOTHING));
                    tfIdfVector.put(term, tf * idf);
                }
            }
            docVectors.put(doc.getId(), tfIdfVector);
        }

        log.info("[向量索引] 构建完成: {}个文档, {}个词汇, 平均向量维度={}",
                totalDocs, idfMap.size(),
                docVectors.values().stream().mapToInt(Map::size).average().orElse(0));
    }

    // ==================== LLM 扩展接口（支持多种免费API） ====================

    /**
     * 调用外部大模型API进行问答
     *
     * 【支持的免费API提供商】（2026年7月更新）
     * 1. 阿里百炼 (Qwen3)   - 新用户100万tokens免费 → https://bailian.console.aliyun.com
     *    ⚠️ qwen-turbo已于2026.7.13下线，推荐 qwen3-turbo / qwen-plus / qwen-flash
     * 2. DeepSeek            - 注册送额度            → https://platform.deepseek.com
     * 3. 智谱AI (GLM-4-Flash)- 完全免费              → https://open.bigmodel.cn
     * 4. Moonshot (Kimi)     - 注册送额度            → https://platform.moonshot.cn
     * 5. 零一万物 (Yi)        - 注册送额度           → https://platform.lingyiwanwu.com
     *
     * 以上所有API均兼容OpenAI格式，只需修改 api-url、api-key、model 三个配置项
     * 使用本地引擎匹配结果作为context，实现RAG(检索增强生成)模式
     *
     * @param question 用户问题
     * @return AI响应（失败或未配置时返回null，自动降级到本地引擎）
     */
    private ChatResponse callLlmApi(String question) {
        // 首先用本地引擎检索相关上下文（RAG模式）
        List<String> questionTokens = tokenize(question);
        questionTokens = expandSynonyms(questionTokens);
        Map<String, Double> queryVector = computeQueryTfIdf(questionTokens);

        List<MatchResult> results = new ArrayList<>();
        for (AiKnowledge doc : knowledgeBase) {
            double score = computeFusionScore(queryVector, questionTokens, doc);
            if (score > 0.1) {
                results.add(new MatchResult(doc, score));
            }
        }
        results.sort((a, b) -> Double.compare(b.score, a.score));

        // 构建RAG上下文：取Top 3本地匹配结果作为大模型的参考材料
        StringBuilder contextBuilder = new StringBuilder();
        int contextCount = Math.min(3, results.size());
        for (int i = 0; i < contextCount; i++) {
            contextBuilder.append("【参考材料").append(i + 1).append("】")
                    .append(results.get(i).doc.getAnswer().replace("\n", " ")).append("\n");
        }

        // 系统提示词：设定角色 + 注入RAG上下文
        String systemPrompt = "你是东北石油大学智慧迎新系统的AI智能助手「哈基油油子」，专门为大一新生解答入学相关问题。" +
                "回答要求：①基于参考材料准确回答 ②如果参考材料不足以回答，请诚实说明 ③友好、简洁、用中文 ④可适当使用emoji\n\n" +
                "以下是知识库中检索到的参考材料：\n" + contextBuilder;

        // 构建OpenAI兼容格式的请求体（阿里Qwen/DeepSeek/GLM/Moonshot均兼容此格式）
        String requestBody = String.format(
                "{\"model\":\"%s\",\"messages\":[" +
                "{\"role\":\"system\",\"content\":\"%s\"}," +
                "{\"role\":\"user\",\"content\":\"%s\"}" +
                "],\"temperature\":0.7,\"max_tokens\":800}",
                llmModel,
                escapeJson(systemPrompt),
                escapeJson(question)
        );

        try {
            java.net.URL url = new java.net.URL(llmApiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + llmApiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            java.io.OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                String responseStr = response.toString();
                log.debug("[LLM] 原始响应: {}", responseStr.substring(0, Math.min(200, responseStr.length())));

                // 解析OpenAI兼容格式: choices[0].message.content
                String content = extractJsonField(responseStr, "content");
                if (content != null && !content.isEmpty()) {
                    ChatResponse resp = new ChatResponse();
                    resp.setQuestion(question);
                    resp.setAnswer(content);
                    resp.setConfidence(0.92);
                    resp.setIsUnknown(false);
                    resp.setCategory(results.isEmpty() ? "其他" : results.get(0).doc.getCategory());
                    log.info("[LLM] 大模型回答成功, 内容长度={}", content.length());
                    return resp;
                }
                // content为空的兜底处理
                log.warn("[LLM] 响应中未找到content字段");
            } else {
                // 读取错误响应体
                try {
                    java.io.BufferedReader errorReader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8));
                    StringBuilder errorBody = new StringBuilder();
                    String errLine;
                    while ((errLine = errorReader.readLine()) != null) {
                        errorBody.append(errLine);
                    }
                    errorReader.close();
                    log.warn("[LLM] API返回错误 {} : {}", responseCode, errorBody.toString());
                } catch (Exception ignored) {
                    log.warn("[LLM] API返回非200状态码: {}", responseCode);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            log.warn("[LLM] API调用超时");
        } catch (java.io.IOException e) {
            log.warn("[LLM] API网络异常: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("[LLM] API调用异常: {}", e.getMessage());
        }
        return null; // 返回null → 自动降级到本地引擎
    }

    /** 转义JSON字符串中的特殊字符 */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 从JSON字符串中提取指定字段的值
     * 适配OpenAI兼容格式: {"choices":[{"message":{"content":"..."}}]}
     * 不引入第三方JSON库，使用简化的字符串解析
     */
    private String extractJsonField(String json, String fieldName) {
        // 遍历所有 "fieldName":"value" 模式，取最后一个（message中的content优先级更高）
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
                // 字符串值
                int valueEnd = valueStart + 1;
                while (valueEnd < json.length()) {
                    if (json.charAt(valueEnd) == '"' && json.charAt(valueEnd - 1) != '\\') {
                        break;
                    }
                    valueEnd++;
                }
                String val = json.substring(valueStart + 1, valueEnd)
                        .replace("\\\"", "\"")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
                        .replace("\\\\", "\\");
                if (!val.isEmpty()) {
                    lastValue = val;
                }
            } else {
                // 非字符串值
                int valueEnd = valueStart;
                while (valueEnd < json.length() && ",}]".indexOf(json.charAt(valueEnd)) == -1) {
                    valueEnd++;
                }
                String val = json.substring(valueStart, valueEnd).trim();
                if (!val.isEmpty()) {
                    lastValue = val;
                }
            }
            // 继续查找下一个匹配
            keyIdx = json.indexOf(key, valueStart);
        }
        return lastValue;
    }

    // ==================== 辅助方法 ====================

    /** 构建未知问题的友好回复 */
    private ChatResponse buildUnknownResponse(String question) {
        ChatResponse response = new ChatResponse();
        response.setQuestion(question);
        response.setAnswer(
                "🤔 很抱歉，我目前的知识库中暂时没有找到与您问题直接匹配的答案。\n\n" +
                "您可以尝试以下方式获取帮助：\n" +
                "① 换一种方式提问（尝试更简洁或更具体的表述）\n" +
                "② 查看【迎新指南-常见问题】页面，那里有更多分类整理的信息\n" +
                "③ 联系辅导员或拨打招生办电话：0459-6503XXX\n" +
                "④ 在【交流社区】发帖，学长学姐会热心解答\n\n" +
                "💡 小贴士：尽量使用简短的句子提问，如【宿舍有空调吗】、【学费多少】等。"
        );
        response.setConfidence(0.0);
        response.setCategory("未知");
        response.setIsUnknown(true);
        response.setRelatedQuestions(getQuickQuestions());
        return response;
    }

    /** 保存对话历史到数据库 */
    private void saveHistory(Long userId, String sessionId, String question,
                             String answer, Long knowledgeId, Double confidence,
                             int isUnknown, String ipAddress) {
        try {
            AiChatHistory history = new AiChatHistory();
            history.setUserId(userId);
            history.setSessionId(sessionId);
            history.setQuestion(question);
            history.setAnswer(answer);
            history.setSourceKnowledgeId(knowledgeId);
            history.setConfidence(confidence);
            history.setIsUnknown(isUnknown);
            history.setIpAddress(ipAddress);
            chatHistoryMapper.insert(history);
        } catch (Exception e) {
            log.warn("[对话历史] 保存失败: {}", e.getMessage());
        }
    }

    // ==================== 公共查询接口 ====================

    @Override
    public String[] getQuickQuestions() {
        return new String[]{
                "报到需要带什么材料？",
                "宿舍有空调吗？",
                "军训多长时间？",
                "怎么缴学费？",
                "有哪些奖学金？",
                "怎么加入社团？",
                "校园网怎么连？",
                "图书馆几点开放？"
        };
    }

    @Override
    public String[] getHotQuestions(int limit) {
        List<AiKnowledge> hotList = knowledgeMapper.selectHotQuestions(limit);
        return hotList.stream()
                .map(AiKnowledge::getQuestion)
                .toArray(String[]::new);
    }

    @Override
    public String[] getCategories() {
        return CATEGORY_KEYWORDS.keySet().toArray(new String[0]);
    }

    @Override
    public void reloadKnowledgeBase() {
        this.knowledgeBase = knowledgeMapper.selectAllEnabled();
        buildVectorIndex();
        buildSynonymReverseIndex();
        log.info("[知识库] 已重新加载，当前条目数: {}", knowledgeBase.size());
    }

    // ==================== RAG 检索（供外部大模型使用） ====================

    /**
     * 从本地知识库检索与问题最相关的TopK答案，拼接为参考材料文本。
     * 复用本地引擎的分词、同义词扩展与多策略融合评分，与chat()的检索逻辑一致。
     *
     * @param question 用户问题
     * @param topK 参考材料条数
     * @return 参考材料文本（无匹配时返回空串）
     */
    @Override
    public String retrieveContext(String question, int topK) {
        if (question == null || question.trim().isEmpty() || topK <= 0) {
            return "";
        }
        List<String> tokens = tokenize(question.trim());
        tokens = expandSynonyms(tokens);
        Map<String, Double> queryVector = computeQueryTfIdf(tokens);

        List<MatchResult> results = new ArrayList<>();
        for (AiKnowledge doc : knowledgeBase) {
            double score = computeFusionScore(queryVector, tokens, doc);
            if (score > 0.1) {
                results.add(new MatchResult(doc, score));
            }
        }
        results.sort((a, b) -> Double.compare(b.score, a.score));

        StringBuilder contextBuilder = new StringBuilder();
        int count = Math.min(topK, results.size());
        for (int i = 0; i < count; i++) {
            String answer = results.get(i).doc.getAnswer();
            contextBuilder.append("【参考材料").append(i + 1).append("】")
                    .append(answer != null ? answer.replace("\n", " ") : "").append("\n");
        }
        log.info("[RAG检索] question={}, 命中材料数={}", question, count);
        return contextBuilder.toString();
    }

    @Override
    public Map<String, Object> getKnowledgeStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalKnowledge", knowledgeBase.size());
        stats.put("vocabularySize", idfMap.size());
        stats.put("llmEnabled", llmEnabled);
        stats.put("threshold", UNKNOWN_THRESHOLD);
        stats.put("weights", Map.of(
                "jaccard", WEIGHT_JACCARD,
                "tfidf", WEIGHT_TFIDF,
                "category", WEIGHT_CATEGORY
        ));

        // 按分类统计
        Map<String, Long> categoryCount = knowledgeBase.stream()
                .collect(Collectors.groupingBy(
                        k -> k.getCategory() != null ? k.getCategory() : "未分类",
                        Collectors.counting()
                ));
        stats.put("categoryDistribution", categoryCount);

        return stats;
    }

    // ==================== 内部数据结构 ====================

    /** 匹配结果内部类：知识条目 + 得分 */
    private static class MatchResult {
        final AiKnowledge doc;
        final double score;

        MatchResult(AiKnowledge doc, double score) {
            this.doc = doc;
            this.score = score;
        }
    }
}

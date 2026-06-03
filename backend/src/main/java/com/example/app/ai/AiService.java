package com.example.app.ai;

import com.example.app.dto.DiagnosisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class AiService {

    @Value("${ai.ecnu.api-key:}")
    private String apiKey;

    @Value("${ai.ecnu.base-url:https://chat.ecnu.edu.cn/open/api/v1}")
    private String baseUrl;

    @Value("${ai.ecnu.model:ChatECNU}")
    private String model;

    // OCR 功能开关
    private boolean ocrEnabled = true;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用ChatECNU API
     */
    public String callChatECNU(String prompt) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                log.error("API Key未配置");
                return "AI服务调用失败: API Key未配置，请在application.yml中设置ai.ecnu.api-key或通过环境变量AI_ECNU_API_KEY配置";
            }

            String url = baseUrl + "/chat/completions";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            // 解析OpenAI标准格式的响应
            if (jsonNode.has("choices") && jsonNode.get("choices").isArray()) {
                JsonNode firstChoice = jsonNode.get("choices").get(0);
                if (firstChoice.has("message")) {
                    return firstChoice.get("message").get("content").asText();
                }
            }
            
            log.error("无法解析API响应: {}", response.getBody());
            return "AI服务响应解析失败";
        } catch (Exception e) {
            log.error("调用ChatECNU失败", e);
            return "AI服务调用失败: " + e.getMessage();
        }
    }

    /**
     * OCR识别图片文字 - 使用千问多模态AI模型识别
     */
    public String ocrImage(String imageBase64) {
        if (!ocrEnabled) {
            log.warn("OCR功能未启用");
            return "";
        }

        try {
            if (apiKey == null || apiKey.isEmpty()) {
                log.error("API Key未配置");
                return "OCR服务未配置";
            }

            String url = baseUrl + "/chat/completions";

            // 构建多模态请求，参考千问API文档格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "ecnu-vl"); // 使用多模态模型
            requestBody.put("stream", false);

            // 构建消息列表，包含system和user消息
            List<Map<String, Object>> messages = new ArrayList<>();

            // System消息
            messages.add(Map.of(
                "role", "system",
                "content", "你是一个支持多模态理解的大模型，擅长识别图片中的文字内容。"
            ));

            // 构建用户消息内容（包含文本和图片）
            List<Map<String, Object>> content = new ArrayList<>();

            // 添加文本提示
            content.add(Map.of(
                "type", "text",
                "text", "请识别这张图片中的文字内容。如果是数学题，请完整提取题目内容；如果是解答过程，请完整提取解答步骤。只返回识别到的文字，不要其他说明。"
            ));

            // 添加图片，使用base64格式
            content.add(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:image/jpeg;base64," + imageBase64)
            ));

            // User消息
            messages.add(Map.of(
                "role", "user",
                "content", content
            ));

            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("发送OCR请求到千问多模态模型...");
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            // 解析OpenAI标准格式的响应
            if (jsonNode.has("choices") && jsonNode.get("choices").isArray()) {
                JsonNode firstChoice = jsonNode.get("choices").get(0);
                if (firstChoice.has("message")) {
                    String result = firstChoice.get("message").get("content").asText();
                    log.info("OCR识别成功，结果长度: {}", result.length());
                    return result;
                }
            }

            log.error("无法解析OCR响应: {}", response.getBody());
            return "OCR识别失败：无法解析响应";

        } catch (Exception e) {
            log.error("OCR识别失败", e);
            return "OCR识别失败：" + e.getMessage() + "。请手动输入题目内容。";
        }
    }

    /**
     * 生成结构化笔记
     */
    public String generateNotes(String theme, String concepts, String content) {
        // 构建提示词，根据用户是否提供主题和概念来调整
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一位专业的学习助手。请根据以下内容生成一份结构化的学习笔记。\n\n");

        // 如果用户提供了主题，使用用户提供的；否则让AI提炼
        if (theme != null && !theme.trim().isEmpty()) {
            promptBuilder.append("核心主题：").append(theme).append("\n");
        } else {
            promptBuilder.append("核心主题：[请根据内容自动提炼主题]\n");
        }

        // 如果用户提供了重点概念，使用用户提供的；否则让AI识别
        if (concepts != null && !concepts.trim().isEmpty()) {
            promptBuilder.append("重点概念：").append(concepts).append("\n");
        } else {
            promptBuilder.append("重点概念：[请根据内容自动识别重点概念]\n");
        }

        promptBuilder.append("原始内容：").append(content).append("\n\n");

        promptBuilder.append("""
            请按照以下Markdown格式生成笔记：

            # [主题名称]

            ## 核心定义
            [提取关键定义和概念]

            ## 关键要点
            [列出重要知识点及简要说明]

            ## 重点概念解析
            [详细解释重点概念，如果用户未指定，请自动识别并解释]

            ## 易混淆点对比
            [对比容易混淆的概念或知识点]

            ## 应用要点
            [总结实际应用场景和注意事项]

            注意：
            1. 如果用户未提供主题，请根据内容自动提炼一个合适的主题
            2. 如果用户未提供重点概念，请自动识别内容中的关键概念并详细解释
            3. 笔记应该结构清晰、层次分明
            4. 使用通俗易懂的语言，适当使用列表和表格
            """);

        return callChatECNU(promptBuilder.toString());
    }

    /**
     * 苏格拉底式对话
     */
    public String socraticDialogue(String question, List<String> history) {
        StringBuilder historyContext = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            historyContext.append("历史对话：\n");
            for (String h : history) {
                historyContext.append(h).append("\n");
            }
        }

        String prompt = String.format("""
            你是一位亲切的数学学习伙伴，像朋友一样帮助学生理解数学概念。

            %s

            学生问题：%s

            回答要求：
            1. 用通俗易懂的语言解释，避免过于学术化的套话
            2. 多用生活中的例子或类比来说明抽象概念
            3. 回答结构清晰，适当分点说明
            4. 语气友好亲切，可以适当使用emoji增加亲和力 😊
            5. 先直接回答核心问题，再用引导性问题帮助学生深入思考
            6. 可以问："你觉得这个和之前学的有什么联系？"、"能举个例子吗？"、"如果是这种情况会怎样？"

            请用中文回答，像朋友聊天一样自然。
            """, historyContext.toString(), question);

        return callChatECNU(prompt);
    }

    /**
     * 诊断解题错误
     */
    public DiagnosisResult diagnoseSolution(String question, String userAnswer) {
        // 输入验证
        if (question == null || question.trim().isEmpty()) {
            return DiagnosisResult.builder()
                .errorType("输入错误")
                .knowledgePoints("无法识别")
                .feedback("题目内容不能为空，请输入完整的题目内容。")
                .hintQuestion("请检查是否已正确输入题目？")
                .similarExample("暂无")
                .fullSolution("暂无")
                .build();
        }

        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return DiagnosisResult.builder()
                .errorType("输入错误")
                .knowledgePoints("无法识别")
                .feedback("解答内容不能为空，请输入你的解答过程。")
                .hintQuestion("请检查是否已正确输入解答？")
                .similarExample("暂无")
                .fullSolution("暂无")
                .build();
        }

        String prompt = String.format("""
            你是一位数学解题诊断专家。请分析学生的解答，识别错误类型并给出反馈。

            题目：
            %s

            学生解答：
            %s

            请严格按以下JSON格式返回分析结果（不要添加markdown代码块标记，直接返回JSON）：
            {
                "errorType": "错误类型，必须是以下之一：概念误解型/计算疏忽型/迁移困难型/逻辑断层型/无错误",
                "knowledgePoints": "涉及的知识点，用简短词语描述",
                "feedback": "对错误的简要说明，用通俗易懂的语言",
                "hintQuestion": "引导学生自己发现错误的提示性问题",
                "similarExample": "一个相似的简化例题，包含题目和解答",
                "fullSolution": "完整的正确解答，步骤清晰"
            }

            注意：
            - 如果解答正确，errorType必须填"无错误"
            - 重点关注"迁移困难型"错误（未能将当前问题与已学例题的方法关联）
            - feedback不要直接给答案，而是引导性提示
            - 所有字段都必须有值，不能为空字符串
            - 如果无法判断错误类型，errorType填"未知"

            只返回JSON，不要其他内容。
            """, question.trim(), userAnswer.trim());

        String result = callChatECNU(prompt);
        log.debug("AI诊断返回结果: {}", result);

        // 检查AI服务是否返回错误
        if (result.startsWith("AI服务") || result.startsWith("AI服务调用失败")) {
            return DiagnosisResult.builder()
                .errorType("服务异常")
                .knowledgePoints("无法识别")
                .feedback("AI服务暂时不可用，请稍后重试。错误信息：" + result)
                .hintQuestion("请稍后再试，或联系管理员检查AI服务配置。")
                .similarExample("暂无")
                .fullSolution("暂无")
                .build();
        }

        try {
            // 清理结果，去除可能的markdown代码块标记
            String cleanedResult = result.trim();
            if (cleanedResult.startsWith("```json")) {
                cleanedResult = cleanedResult.substring(7);
            } else if (cleanedResult.startsWith("```")) {
                cleanedResult = cleanedResult.substring(3);
            }
            if (cleanedResult.endsWith("```")) {
                cleanedResult = cleanedResult.substring(0, cleanedResult.length() - 3);
            }
            cleanedResult = cleanedResult.trim();

            // 提取JSON部分
            int jsonStart = cleanedResult.indexOf("{");
            int jsonEnd = cleanedResult.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = cleanedResult.substring(jsonStart, jsonEnd);
                JsonNode jsonNode = objectMapper.readTree(jsonStr);

                // 验证所有必需字段
                String errorType = getJsonNodeText(jsonNode, "errorType", "未知");
                String knowledgePoints = getJsonNodeText(jsonNode, "knowledgePoints", "暂无识别结果");
                String feedback = getJsonNodeText(jsonNode, "feedback", "暂无反馈信息");
                String hintQuestion = getJsonNodeText(jsonNode, "hintQuestion", "");
                String similarExample = getJsonNodeText(jsonNode, "similarExample", "暂无");
                String fullSolution = getJsonNodeText(jsonNode, "fullSolution", "暂无");

                // 验证错误类型是否有效
                List<String> validErrorTypes = List.of("概念误解型", "计算疏忽型", "迁移困难型", "逻辑断层型", "无错误", "未知");
                if (!validErrorTypes.contains(errorType)) {
                    errorType = "未知";
                }

                return DiagnosisResult.builder()
                    .errorType(errorType)
                    .knowledgePoints(knowledgePoints)
                    .feedback(feedback)
                    .hintQuestion(hintQuestion)
                    .similarExample(similarExample)
                    .fullSolution(fullSolution)
                    .build();
            } else {
                log.error("无法从响应中提取JSON: {}", cleanedResult);
            }
        } catch (Exception e) {
            log.error("解析诊断结果失败, 原始结果: {}", result, e);
        }

        // 返回解析失败但友好的结果
        String truncatedResult = result.length() > 150 ? result.substring(0, 150) + "..." : result;
        return DiagnosisResult.builder()
            .errorType("解析失败")
            .knowledgePoints("无法识别")
            .feedback("AI返回的结果格式不正确，但已尽力提取可用信息。原始返回：" + truncatedResult)
            .hintQuestion("请检查题目和解答是否输入完整，然后重新诊断。")
            .similarExample("暂无")
            .fullSolution("暂无")
            .build();
    }

    /**
     * 安全获取JSON节点文本
     */
    private String getJsonNodeText(JsonNode jsonNode, String fieldName, String defaultValue) {
        if (jsonNode.has(fieldName) && !jsonNode.get(fieldName).isNull()) {
            return jsonNode.get(fieldName).asText(defaultValue);
        }
        return defaultValue;
    }

    /**
     * 生成个性化练习题
     */
    public String generateExam(String knowledgePoint, String errorType) {
        String prompt = String.format("""
            你是一位个性化学习教练。请根据学生的薄弱点生成一道练习题。

            薄弱知识点：%s
            主要错误类型：%s

            请生成一道针对性的练习题，并说明：
            1. 本题旨在训练什么能力
            2. 解题思路提示
            3. 参考答案

            如果是"迁移困难型"错误，请设计一道需要运用已学方法解决新问题的题目。
            """, knowledgePoint, errorType);

        return callChatECNU(prompt);
    }

    /**
     * 生成文本内容（通用方法）
     */
    public String generateText(String prompt) {
        return callChatECNU(prompt);
    }

    /**
     * 判断学生解答是否正确
     * @return 包含isCorrect(是否正确)和correctAnswer(正确答案)的结果
     */
    public java.util.Map<String, Object> checkAnswerCorrectness(String question, String userAnswer) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        if (question == null || question.trim().isEmpty() ||
            userAnswer == null || userAnswer.trim().isEmpty()) {
            result.put("isCorrect", false);
            result.put("correctAnswer", "无法判断");
            result.put("analysis", "题目或解答内容为空");
            return result;
        }

        String prompt = String.format("""
            你是一位数学解题专家。请判断学生的解答是否正确。

            题目：
            %s

            学生解答：
            %s

            请严格按以下JSON格式返回判断结果（不要添加markdown代码块标记，直接返回JSON）：
            {
                "isCorrect": true/false,
                "correctAnswer": "完整的正确答案和解题过程",
                "analysis": "对学生解答的简要分析，指出正确或错误的关键点"
            }

            注意：
            - isCorrect必须是true或false
            - 如果学生解答正确，isCorrect为true
            - 如果学生解答有错误（包括计算错误、概念错误、逻辑错误等），isCorrect为false
            - correctAnswer要给出完整、规范的解答过程
            - analysis要简洁明了，指出学生解答的问题所在

            只返回JSON，不要其他内容。
            """, question.trim(), userAnswer.trim());

        String aiResponse = callChatECNU(prompt);
        log.debug("AI判断解答正确性返回结果: {}", aiResponse);

        // 检查AI服务是否返回错误
        if (aiResponse.startsWith("AI服务") || aiResponse.startsWith("AI服务调用失败")) {
            result.put("isCorrect", false);
            result.put("correctAnswer", "无法获取");
            result.put("analysis", "AI服务暂时不可用：" + aiResponse);
            return result;
        }

        try {
            // 清理结果，去除可能的markdown代码块标记
            String cleanedResult = aiResponse.trim();
            if (cleanedResult.startsWith("```json")) {
                cleanedResult = cleanedResult.substring(7);
            } else if (cleanedResult.startsWith("```")) {
                cleanedResult = cleanedResult.substring(3);
            }
            if (cleanedResult.endsWith("```")) {
                cleanedResult = cleanedResult.substring(0, cleanedResult.length() - 3);
            }
            cleanedResult = cleanedResult.trim();

            // 提取JSON部分
            int jsonStart = cleanedResult.indexOf("{");
            int jsonEnd = cleanedResult.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = cleanedResult.substring(jsonStart, jsonEnd);
                JsonNode jsonNode = objectMapper.readTree(jsonStr);

                boolean isCorrect = jsonNode.has("isCorrect") ? jsonNode.get("isCorrect").asBoolean(false) : false;
                String correctAnswer = getJsonNodeText(jsonNode, "correctAnswer", "暂无");
                String analysis = getJsonNodeText(jsonNode, "analysis", "暂无分析");

                result.put("isCorrect", isCorrect);
                result.put("correctAnswer", correctAnswer);
                result.put("analysis", analysis);
                return result;
            }
        } catch (Exception e) {
            log.error("解析AI判断结果失败, 原始结果: {}", aiResponse, e);
        }

        // 解析失败时的默认返回
        result.put("isCorrect", false);
        result.put("correctAnswer", "解析失败");
        result.put("analysis", "无法解析AI返回结果");
        return result;
    }
}

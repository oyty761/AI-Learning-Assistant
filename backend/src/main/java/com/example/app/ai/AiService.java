package com.example.app.ai;

import com.example.app.dto.DiagnosisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiService {

    @Value("${ai.baidu.api-key:}")
    private String apiKey;

    @Value("${ai.baidu.secret-key:}")
    private String secretKey;

    @Value("${ai.baidu.ocr-api-key:}")
    private String ocrApiKey;

    @Value("${ai.baidu.ocr-secret-key:}")
    private String ocrSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private long tokenExpireTime;

    /**
     * 获取百度AI访问令牌
     */
    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        try {
            String url = String.format(
                "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s",
                apiKey, secretKey
            );

            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            accessToken = jsonNode.get("access_token").asText();
            long expiresIn = jsonNode.get("expires_in").asLong();
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000;
            return accessToken;
        } catch (Exception e) {
            log.error("获取access token失败", e);
            return null;
        }
    }

    /**
     * 调用文心一言API
     */
    public String callWenxin(String prompt) {
        try {
            String token = getAccessToken();
            if (token == null) {
                return "AI服务暂时不可用，请稍后重试";
            }

            String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro?access_token=" + token;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("result").asText();
        } catch (Exception e) {
            log.error("调用文心一言失败", e);
            return "AI服务调用失败: " + e.getMessage();
        }
    }

    /**
     * OCR识别图片文字
     */
    public String ocrImage(String imageBase64) {
        try {
            String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
            
            String token = getAccessToken();
            if (token == null) {
                return "";
            }

            url += "?access_token=" + token;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "image=" + imageBase64;
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            StringBuilder result = new StringBuilder();
            JsonNode wordsResult = jsonNode.get("words_result");
            if (wordsResult != null && wordsResult.isArray()) {
                for (JsonNode word : wordsResult) {
                    result.append(word.get("words").asText()).append("\n");
                }
            }
            return result.toString().trim();
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            return "";
        }
    }

    /**
     * 生成结构化笔记
     */
    public String generateNotes(String theme, String concepts, String content) {
        String prompt = String.format("""
            你是一位数学思维课程的学习助手。请根据以下内容生成一份结构化的学习笔记。
            
            核心主题：%s
            重点概念：%s
            原始内容：%s
            
            请按照以下Markdown格式生成笔记：
            
            # %s
            
            ## 核心定义
            [提取关键定义]
            
            ## 关键定理
            [列出重要定理及简要说明]
            
            ## 典型例题
            [总结典型例题类型]
            
            ## 易混淆点对比
            [对比容易混淆的概念]
            
            ## 应用要点
            [从抽象到具体的迁移要点]
            """, theme, concepts, content, theme);

        return callWenxin(prompt);
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
            你是一位苏格拉底式的数学导师。请回答学生的问题，并引导他们深入思考。
            
            %s
            
            学生问题：%s
            
            回答策略：
            1. 先直接回答学生的问题
            2. 然后提出一个引导性问题，帮助学生深入理解
            3. 可以问："你为什么会想到这个定理？"、"这个结论与之前学过的有什么异同？"、"你能举一个具体的例子吗？"
            
            请用中文回答。
            """, historyContext.toString(), question);

        return callWenxin(prompt);
    }

    /**
     * 诊断解题错误
     */
    public DiagnosisResult diagnoseSolution(String question, String userAnswer) {
        String prompt = String.format("""
            你是一位数学解题诊断专家。请分析学生的解答，识别错误类型并给出反馈。
            
            题目：
            %s
            
            学生解答：
            %s
            
            请按以下JSON格式返回分析结果：
            {
                "errorType": "错误类型（概念误解型/计算疏忽型/迁移困难型/逻辑断层型/无错误）",
                "knowledgePoints": "涉及的知识点",
                "feedback": "对错误的简要说明",
                "hintQuestion": "引导学生自己发现错误的提示性问题",
                "similarExample": "一个相似的简化例题",
                "fullSolution": "完整的正确解答"
            }
            
            注意：
            - 如果解答正确，errorType填"无错误"
            - 重点关注"迁移困难型"错误（未能将当前问题与已学例题的方法关联）
            - 反馈不要直接给答案，而是引导性提示
            
            只返回JSON，不要其他内容。
            """, question, userAnswer);

        String result = callWenxin(prompt);
        
        try {
            // 提取JSON部分
            int jsonStart = result.indexOf("{");
            int jsonEnd = result.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = result.substring(jsonStart, jsonEnd);
                JsonNode jsonNode = objectMapper.readTree(jsonStr);
                
                return DiagnosisResult.builder()
                    .errorType(jsonNode.get("errorType").asText())
                    .knowledgePoints(jsonNode.get("knowledgePoints").asText())
                    .feedback(jsonNode.get("feedback").asText())
                    .hintQuestion(jsonNode.get("hintQuestion").asText())
                    .similarExample(jsonNode.get("similarExample").asText())
                    .fullSolution(jsonNode.get("fullSolution").asText())
                    .build();
            }
        } catch (Exception e) {
            log.error("解析诊断结果失败", e);
        }

        // 返回默认结果
        return DiagnosisResult.builder()
            .errorType("未知")
            .knowledgePoints("")
            .feedback("无法准确分析，请检查输入内容")
            .hintQuestion("")
            .similarExample("")
            .fullSolution("")
            .build();
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

        return callWenxin(prompt);
    }
}

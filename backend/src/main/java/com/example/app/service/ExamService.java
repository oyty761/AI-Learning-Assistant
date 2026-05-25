package com.example.app.service;

import com.example.app.ai.AiService;
import com.example.app.entity.UserErrorProfile;
import com.example.app.repository.UserErrorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final AiService aiService;
    private final UserErrorProfileRepository userErrorProfileRepository;

    /**
     * 生成个性化练习题
     */
    public String generatePersonalizedExam(String userId) {
        // 获取用户的薄弱知识点
        List<UserErrorProfile> weakPoints = userErrorProfileRepository
            .findByUserIdOrderByErrorCountDesc(userId);

        if (weakPoints.isEmpty()) {
            return "暂无错误记录，请先使用解题诊断功能。";
        }

        // 选择错误最多的知识点
        UserErrorProfile target = weakPoints.get(0);
        
        // 生成针对性练习
        return aiService.generateExam(target.getKnowledgePoint(), target.getErrorType());
    }

    /**
     * 针对特定知识点生成练习
     */
    public String generateExamForKnowledgePoint(String knowledgePoint, String errorType) {
        return aiService.generateExam(knowledgePoint, errorType);
    }

    /**
     * 获取用户学习档案统计
     */
    public Map<String, Object> getUserProfile(String userId) {
        List<UserErrorProfile> profiles = userErrorProfileRepository.findByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalErrors", profiles.size());
        result.put("profiles", profiles);
        
        // 计算掌握度评分
        int totalCount = profiles.stream().mapToInt(UserErrorProfile::getErrorCount).sum();
        result.put("totalErrorCount", totalCount);
        
        // 找出需要重点关注的知识点（错误次数>=2）
        List<UserErrorProfile> needAttention = profiles.stream()
            .filter(p -> p.getErrorCount() >= 2)
            .toList();
        result.put("needAttention", needAttention);
        
        return result;
    }

    /**
     * 检查是否需要推荐练习
     */
    public boolean shouldRecommendPractice(String userId) {
        List<UserErrorProfile> profiles = userErrorProfileRepository
            .findByUserIdOrderByErrorCountDesc(userId);
        
        // 如果有知识点错误次数>=2，推荐练习
        return profiles.stream().anyMatch(p -> p.getErrorCount() >= 2);
    }
}

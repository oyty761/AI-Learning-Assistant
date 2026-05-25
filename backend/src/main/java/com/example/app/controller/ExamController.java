package com.example.app.controller;

import com.example.app.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExamController {

    private final ExamService examService;

    /**
     * 生成个性化练习题
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateExam(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        
        if (userId == null) {
            return ResponseEntity.badRequest().body("缺少userId参数");
        }

        try {
            String exam = examService.generatePersonalizedExam(userId);
            Map<String, String> result = new HashMap<>();
            result.put("exam", exam);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成练习失败: " + e.getMessage());
        }
    }

    /**
     * 针对特定知识点生成练习
     */
    @PostMapping("/generate-specific")
    public ResponseEntity<?> generateSpecificExam(@RequestBody Map<String, String> request) {
        String knowledgePoint = request.get("knowledgePoint");
        String errorType = request.get("errorType");
        
        if (knowledgePoint == null || errorType == null) {
            return ResponseEntity.badRequest().body("缺少必要参数");
        }

        try {
            String exam = examService.generateExamForKnowledgePoint(knowledgePoint, errorType);
            Map<String, String> result = new HashMap<>();
            result.put("exam", exam);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成练习失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户学习档案
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {
        Map<String, Object> profile = examService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * 检查是否需要推荐练习
     */
    @GetMapping("/should-recommend")
    public ResponseEntity<?> shouldRecommend(@RequestParam String userId) {
        boolean shouldRecommend = examService.shouldRecommendPractice(userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("shouldRecommend", shouldRecommend);
        return ResponseEntity.ok(result);
    }
}

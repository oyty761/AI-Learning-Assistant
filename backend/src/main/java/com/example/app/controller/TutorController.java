package com.example.app.controller;

import com.example.app.entity.QaHistory;
import com.example.app.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TutorController {

    private final TutorService tutorService;

    /**
     * 提问接口
     */
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String question = request.get("question");
        
        if (userId == null || question == null) {
            return ResponseEntity.badRequest().body("缺少必要参数");
        }

        try {
            String answer = tutorService.askQuestion(userId, question);
            Map<String, String> result = new HashMap<>();
            result.put("answer", answer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("问答失败: " + e.getMessage());
        }
    }

    /**
     * 获取问答历史
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam String userId) {
        List<QaHistory> history = tutorService.getHistory(userId);
        return ResponseEntity.ok(history);
    }
}

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
     * 创建新会话并提问
     */
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String question = request.get("question");
        String sessionId = request.get("sessionId");

        if (userId == null || question == null) {
            return ResponseEntity.badRequest().body("缺少必要参数");
        }

        try {
            String answer;
            String newSessionId;

            if (sessionId == null || sessionId.isEmpty()) {
                // 创建新会话
                newSessionId = tutorService.createSession(userId, question);
                // 获取刚创建的会话的第一条消息
                List<QaHistory> messages = tutorService.getSessionMessages(newSessionId);
                answer = messages.isEmpty() ? "" : messages.get(0).getAnswer();
            } else {
                // 继续现有会话
                newSessionId = sessionId;
                answer = tutorService.continueDialogue(userId, sessionId, question);
            }

            Map<String, String> result = new HashMap<>();
            result.put("answer", answer);
            result.put("sessionId", newSessionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("问答失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有会话列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(@RequestParam String userId) {
        List<QaHistory> sessions = tutorService.getSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * 获取特定会话的所有消息
     */
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<?> getSessionMessages(@PathVariable String sessionId) {
        List<QaHistory> messages = tutorService.getSessionMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 获取问答历史（兼容旧接口）
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam String userId) {
        List<QaHistory> history = tutorService.getHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
        try {
            tutorService.deleteSession(sessionId);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }
}

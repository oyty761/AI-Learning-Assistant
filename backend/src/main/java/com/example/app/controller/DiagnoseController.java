package com.example.app.controller;

import com.example.app.entity.ErrorRecord;
import com.example.app.entity.UserErrorProfile;
import com.example.app.service.DiagnoseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnose")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiagnoseController {

    private final DiagnoseService diagnoseService;

    /**
     * 上传图片并OCR识别
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndOcr(@RequestParam("file") MultipartFile file) {
        try {
            String text = diagnoseService.uploadAndOcr(file);
            Map<String, String> result = new HashMap<>();
            result.put("text", text);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
        }
    }

    /**
     * 分析解题错误
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(
            @RequestParam String userId,
            @RequestParam String questionText,
            @RequestParam String userAnswer,
            @RequestParam(required = false) String questionImage,
            @RequestParam(required = false) String answerImage) {
        
        try {
            ErrorRecord record = diagnoseService.diagnose(
                userId, questionText, userAnswer, questionImage, answerImage);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("诊断失败: " + e.getMessage());
        }
    }

    /**
     * 获取诊断记录
     */
    @GetMapping("/records")
    public ResponseEntity<?> getRecords(@RequestParam String userId) {
        List<ErrorRecord> records = diagnoseService.getUserRecords(userId);
        return ResponseEntity.ok(records);
    }

    /**
     * 获取用户错误档案
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam String userId) {
        List<UserErrorProfile> profiles = diagnoseService.getUserErrorProfile(userId);
        return ResponseEntity.ok(profiles);
    }
}

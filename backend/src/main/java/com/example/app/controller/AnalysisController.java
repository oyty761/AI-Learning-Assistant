package com.example.app.controller;

import com.example.app.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * 获取学习数据看板统计
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam String userId) {
        try {
            Map<String, Object> stats = analysisService.getDashboardStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取统计数据失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取学习活跃度日历数据
     */
    @GetMapping("/activity-calendar")
    public ResponseEntity<?> getActivityCalendar(@RequestParam String userId) {
        try {
            List<Map<String, Object>> activities = analysisService.getActivityCalendar(userId);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取活跃度数据失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 生成智能学习报告
     */
    @GetMapping("/report")
    public ResponseEntity<?> generateReport(@RequestParam String userId) {
        try {
            Map<String, Object> report = analysisService.generateSmartReport(userId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "生成报告失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

package com.example.app.service;

import com.example.app.ai.AiService;
import com.example.app.entity.ErrorRecord;
import com.example.app.entity.Note;
import com.example.app.entity.QaHistory;
import com.example.app.entity.UserErrorProfile;
import com.example.app.repository.ErrorRecordRepository;
import com.example.app.repository.NoteRepository;
import com.example.app.repository.QaHistoryRepository;
import com.example.app.repository.TodoRepository;
import com.example.app.repository.UserErrorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ErrorRecordRepository errorRecordRepository;
    private final UserErrorProfileRepository userErrorProfileRepository;
    private final NoteRepository noteRepository;
    private final QaHistoryRepository qaHistoryRepository;
    private final TodoRepository todoRepository;
    private final AiService aiService;

    /**
     * 获取学习数据看板统计
     */
    public Map<String, Object> getDashboardStats(String userId) {
        // 总诊断次数
        long totalDiagnose = errorRecordRepository.countByUserId(userId);
        
        // 本周活跃度（近7天的活动次数）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long weeklyActivity = countWeeklyActivity(userId, weekAgo);
        
        // 薄弱知识点数（错误次数>=2的知识点）
        long weakPoints = userErrorProfileRepository.countByUserIdAndErrorCountGreaterThanEqual(userId, 2);
        
        // 任务完成率
        Map<String, Object> todoStats = getTodoCompletionRate(userId);
        
        // 笔记数量
        long noteCount = noteRepository.countByUserId(userId);
        
        // 问答次数
        long qaCount = qaHistoryRepository.countByUserId(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDiagnose", totalDiagnose);
        stats.put("weeklyActivity", weeklyActivity);
        stats.put("weakPoints", weakPoints);
        stats.put("todoCompletionRate", todoStats.get("rate"));
        stats.put("todoCompleted", todoStats.get("completed"));
        stats.put("todoTotal", todoStats.get("total"));
        stats.put("noteCount", noteCount);
        stats.put("qaCount", qaCount);
        
        return stats;
    }

    /**
     * 获取学习活跃度日历数据
     */
    public List<Map<String, Object>> getActivityCalendar(String userId) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // 获取近6个月的数据
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 收集所有活动记录
        Map<String, Integer> activityMap = new HashMap<>();
        
        // 错题诊断活动
        List<ErrorRecord> errorRecords = errorRecordRepository.findByUserIdAndCreatedAtAfter(userId, sixMonthsAgo);
        for (ErrorRecord record : errorRecords) {
            String date = record.getCreatedAt().format(formatter);
            activityMap.put(date, activityMap.getOrDefault(date, 0) + 1);
        }
        
        // 笔记活动
        List<Note> notes = noteRepository.findByUserIdAndCreatedAtAfter(userId, sixMonthsAgo);
        for (Note note : notes) {
            String date = note.getCreatedAt().format(formatter);
            activityMap.put(date, activityMap.getOrDefault(date, 0) + 1);
        }
        
        // 问答活动
        List<QaHistory> qaHistories = qaHistoryRepository.findByUserIdAndCreatedAtAfter(userId, sixMonthsAgo);
        for (QaHistory qa : qaHistories) {
            String date = qa.getCreatedAt().format(formatter);
            activityMap.put(date, activityMap.getOrDefault(date, 0) + 1);
        }
        
        // 转换为列表格式
        for (Map.Entry<String, Integer> entry : activityMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            activities.add(item);
        }
        
        return activities;
    }

    /**
     * 生成智能学习报告
     */
    public Map<String, Object> generateSmartReport(String userId) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取统计数据
        List<UserErrorProfile> errorProfiles = userErrorProfileRepository.findByUserIdOrderByErrorCountDesc(userId);
        List<ErrorRecord> recentRecords = errorRecordRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        
        // 薄弱点Top5
        List<Map<String, Object>> weakPoints = errorProfiles.stream()
            .limit(5)
            .map(profile -> {
                Map<String, Object> point = new HashMap<>();
                point.put("knowledgePoint", profile.getKnowledgePoint());
                point.put("errorType", profile.getErrorType());
                point.put("errorCount", profile.getErrorCount());
                return point;
            })
            .collect(Collectors.toList());
        
        // 错误类型统计
        Map<String, Long> errorTypeStats = errorProfiles.stream()
            .collect(Collectors.groupingBy(UserErrorProfile::getErrorType, Collectors.counting()));
        
        // 生成AI报告
        String aiReport = generateAiReport(userId, errorProfiles, recentRecords);
        
        report.put("weakPoints", weakPoints);
        report.put("errorTypeStats", errorTypeStats);
        report.put("aiReport", aiReport);
        report.put("generatedAt", LocalDateTime.now().toString());
        
        return report;
    }

    /**
     * 统计本周活跃度
     */
    private long countWeeklyActivity(String userId, LocalDateTime since) {
        long count = 0;
        count += errorRecordRepository.countByUserIdAndCreatedAtAfter(userId, since);
        count += noteRepository.countByUserIdAndCreatedAtAfter(userId, since);
        count += qaHistoryRepository.countByUserIdAndCreatedAtAfter(userId, since);
        return count;
    }

    /**
     * 获取任务完成率
     */
    private Map<String, Object> getTodoCompletionRate(String userId) {
        long total = todoRepository.countByUserId(userId);
        long completed = todoRepository.countByUserIdAndCompleted(userId, true);
        
        double rate = total > 0 ? Math.round((double) completed / total * 100) : 0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("rate", rate);
        return result;
    }

    /**
     * 生成AI学习报告
     */
    private String generateAiReport(String userId, List<UserErrorProfile> errorProfiles, List<ErrorRecord> recentRecords) {
        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下学习数据生成一份个性化的学习分析报告：\n\n");
        
        // 统计信息
        int totalErrors = errorProfiles.size();
        int totalErrorCount = errorProfiles.stream().mapToInt(UserErrorProfile::getErrorCount).sum();
        
        prompt.append("【学习概况】\n");
        prompt.append("- 涉及知识点数量：").append(totalErrors).append("\n");
        prompt.append("- 总错误次数：").append(totalErrorCount).append("\n\n");
        
        // 薄弱点
        if (!errorProfiles.isEmpty()) {
            prompt.append("【薄弱知识点】\n");
            errorProfiles.stream().limit(5).forEach(profile -> {
                prompt.append("- ").append(profile.getKnowledgePoint())
                      .append(" (").append(profile.getErrorType()).append(")")
                      .append(" - 错误").append(profile.getErrorCount()).append("次\n");
            });
            prompt.append("\n");
        }
        
        // 最近诊断
        if (!recentRecords.isEmpty()) {
            prompt.append("【最近诊断记录】\n");
            recentRecords.forEach(record -> {
                prompt.append("- ").append(record.getErrorType())
                      .append("：").append(record.getKnowledgePoints()).append("\n");
            });
            prompt.append("\n");
        }
        
        prompt.append("请生成一份包含以下内容的报告：\n");
        prompt.append("1. 学习概况总结（总体评价、学习强度）\n");
        prompt.append("2. 薄弱点分析（Top3薄弱点及改进建议）\n");
        prompt.append("3. 进步亮点（如果有的话）\n");
        prompt.append("4. 具体的学习建议\n\n");
        prompt.append("要求：语言亲切鼓励，结构清晰，使用Markdown格式。");
        
        try {
            return aiService.generateText(prompt.toString());
        } catch (Exception e) {
            log.error("生成AI报告失败", e);
            return generateFallbackReport(errorProfiles, totalErrors, totalErrorCount);
        }
    }

    /**
     * 生成备用报告（AI失败时使用）
     */
    private String generateFallbackReport(List<UserErrorProfile> errorProfiles, int totalErrors, int totalErrorCount) {
        StringBuilder report = new StringBuilder();
        report.append("## 📊 学习概况\n\n");
        report.append("你目前共涉及 **").append(totalErrors).append("** 个知识点，累计诊断 **")
              .append(totalErrorCount).append("** 次。\n\n");
        
        if (!errorProfiles.isEmpty()) {
            report.append("## ⚠️ 薄弱点分析\n\n");
            report.append("需要重点关注的知识点：\n\n");
            errorProfiles.stream().limit(3).forEach(profile -> {
                report.append("1. **").append(profile.getKnowledgePoint()).append("**\n");
                report.append("   - 错误类型：").append(profile.getErrorType()).append("\n");
                report.append("   - 错误次数：").append(profile.getErrorCount()).append("次\n\n");
            });
            
            report.append("## 💡 学习建议\n\n");
            report.append("1. 针对薄弱知识点进行专项练习\n");
            report.append("2. 定期回顾错题，总结错误规律\n");
            report.append("3. 保持每日学习习惯，持续积累\n");
        } else {
            report.append("## 🎉 学习状态\n\n");
            report.append("暂无错题记录，继续保持良好的学习状态！\n");
        }
        
        return report.toString();
    }
}

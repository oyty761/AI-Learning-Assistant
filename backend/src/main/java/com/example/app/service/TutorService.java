package com.example.app.service;

import com.example.app.ai.AiService;
import com.example.app.entity.QaHistory;
import com.example.app.repository.QaHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {

    private final AiService aiService;
    private final QaHistoryRepository qaHistoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${upload.image-path:D:/uploads/tutor/images/}")
    private String imageUploadPath;

    /**
     * 上传图片到D盘
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // 确保上传目录存在
        Path uploadDir = Paths.get(imageUploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String newFilename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadDir.resolve(newFilename);
        file.transferTo(filePath.toFile());

        log.info("图片上传成功: {}", filePath);

        // 返回访问URL
        return "/api/tutor/images/" + newFilename;
    }

    /**
     * 创建新会话（支持图片）
     */
    public String createSession(String userId, String firstQuestion, List<String> imageUrls) {
        String sessionId = UUID.randomUUID().toString();
        // 生成会话标题（取问题的前20个字符）
        String sessionTitle = firstQuestion.length() > 20
            ? firstQuestion.substring(0, 20) + "..."
            : firstQuestion;

        // 保存第一条消息
        String answer = aiService.socraticDialogue(firstQuestion, new ArrayList<>());

        // 将图片URL列表转为JSON字符串
        String imageUrlsJson = null;
        if (imageUrls != null && !imageUrls.isEmpty()) {
            try {
                imageUrlsJson = objectMapper.writeValueAsString(imageUrls);
            } catch (Exception e) {
                log.error("图片URL序列化失败", e);
            }
        }

        QaHistory qaHistory = QaHistory.builder()
            .userId(userId)
            .sessionId(sessionId)
            .sessionTitle(sessionTitle)
            .question(firstQuestion)
            .answer(answer)
            .imageUrls(imageUrlsJson)
            .build();
        qaHistoryRepository.save(qaHistory);

        return sessionId;
    }

    /**
     * 在现有会话中继续对话（支持图片）
     */
    public String continueDialogue(String userId, String sessionId, String question, List<String> imageUrls) {
        // 获取该会话的所有历史消息
        List<QaHistory> sessionHistory = qaHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // 构建历史对话列表
        List<String> history = new ArrayList<>();
        for (QaHistory h : sessionHistory) {
            history.add("Q: " + h.getQuestion());
            history.add("A: " + h.getAnswer());
        }

        // 调用AI服务
        String answer = aiService.socraticDialogue(question, history);

        // 保存新的问答
        String sessionTitle = sessionHistory.isEmpty()
            ? (question.length() > 20 ? question.substring(0, 20) + "..." : question)
            : sessionHistory.get(0).getSessionTitle();

        // 将图片URL列表转为JSON字符串
        String imageUrlsJson = null;
        if (imageUrls != null && !imageUrls.isEmpty()) {
            try {
                imageUrlsJson = objectMapper.writeValueAsString(imageUrls);
            } catch (Exception e) {
                log.error("图片URL序列化失败", e);
            }
        }

        QaHistory qaHistory = QaHistory.builder()
            .userId(userId)
            .sessionId(sessionId)
            .sessionTitle(sessionTitle)
            .question(question)
            .answer(answer)
            .imageUrls(imageUrlsJson)
            .build();
        qaHistoryRepository.save(qaHistory);

        return answer;
    }

    /**
     * 苏格拉底式问答（兼容旧接口，自动创建新会话）
     */
    public String askQuestion(String userId, String question) {
        // 创建新会话（无图片）
        return createSession(userId, question, null);
    }

    /**
     * 获取用户的所有会话列表
     */
    public List<QaHistory> getSessions(String userId) {
        // 获取用户的所有记录
        List<QaHistory> allHistory = qaHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 按sessionId分组，只保留每个会话的第一条记录
        return allHistory.stream()
            .collect(Collectors.groupingBy(QaHistory::getSessionId))
            .values().stream()
            .map(list -> list.get(0)) // 取每个会话的第一条
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // 按时间倒序
            .collect(Collectors.toList());
    }

    /**
     * 获取特定会话的所有消息
     */
    public List<QaHistory> getSessionMessages(String sessionId) {
        return qaHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * 获取问答历史（兼容旧接口）
     */
    public List<QaHistory> getHistory(String userId) {
        return qaHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        List<QaHistory> sessionMessages = qaHistoryRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        qaHistoryRepository.deleteAll(sessionMessages);
        log.info("删除会话成功: {}", sessionId);
    }
}

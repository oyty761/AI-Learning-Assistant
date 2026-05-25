package com.example.app.service;

import com.example.app.ai.AiService;
import com.example.app.dto.DiagnosisResult;
import com.example.app.entity.ErrorRecord;
import com.example.app.entity.UserErrorProfile;
import com.example.app.repository.ErrorRecordRepository;
import com.example.app.repository.UserErrorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnoseService {

    private final AiService aiService;
    private final ErrorRecordRepository errorRecordRepository;
    private final UserErrorProfileRepository userErrorProfileRepository;

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 上传图片并识别文字
     */
    public String uploadAndOcr(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, fileName);
        Files.createDirectories(filePath.getParent());
        file.transferTo(filePath.toFile());

        // 读取图片并转为base64
        byte[] fileContent = Files.readAllBytes(filePath);
        String base64 = Base64.getEncoder().encodeToString(fileContent);
        
        // OCR识别
        return aiService.ocrImage(base64);
    }

    /**
     * 诊断解题错误
     */
    public ErrorRecord diagnose(String userId, String questionText, String userAnswer, 
                                 String questionImage, String answerImage) {
        // 调用AI诊断
        DiagnosisResult result = aiService.diagnoseSolution(questionText, userAnswer);

        // 保存诊断记录
        ErrorRecord record = ErrorRecord.builder()
            .userId(userId)
            .questionImage(questionImage)
            .answerImage(answerImage)
            .questionText(questionText)
            .userAnswer(userAnswer)
            .errorType(result.getErrorType())
            .knowledgePoints(result.getKnowledgePoints())
            .feedback(result.getFeedback())
            .hintQuestion(result.getHintQuestion())
            .similarExample(result.getSimilarExample())
            .fullSolution(result.getFullSolution())
            .build();

        ErrorRecord savedRecord = errorRecordRepository.save(record);

        // 更新用户错误档案
        if (!"无错误".equals(result.getErrorType()) && !"未知".equals(result.getErrorType())) {
            updateErrorProfile(userId, result.getKnowledgePoints(), result.getErrorType());
        }

        return savedRecord;
    }

    /**
     * 更新用户错误档案
     */
    private void updateErrorProfile(String userId, String knowledgePoints, String errorType) {
        Optional<UserErrorProfile> existing = userErrorProfileRepository
            .findByUserIdAndKnowledgePointAndErrorType(userId, knowledgePoints, errorType);

        if (existing.isPresent()) {
            UserErrorProfile profile = existing.get();
            profile.setErrorCount(profile.getErrorCount() + 1);
            userErrorProfileRepository.save(profile);
        } else {
            UserErrorProfile newProfile = UserErrorProfile.builder()
                .userId(userId)
                .knowledgePoint(knowledgePoints)
                .errorType(errorType)
                .errorCount(1)
                .build();
            userErrorProfileRepository.save(newProfile);
        }
    }

    /**
     * 获取用户的诊断记录
     */
    public List<ErrorRecord> getUserRecords(String userId) {
        return errorRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取用户的错误档案
     */
    public List<UserErrorProfile> getUserErrorProfile(String userId) {
        return userErrorProfileRepository.findByUserIdOrderByErrorCountDesc(userId);
    }
}

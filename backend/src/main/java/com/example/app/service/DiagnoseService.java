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
import java.util.Map;
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
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + sanitizeFileName(originalFileName);

        // 使用绝对路径确保目录存在
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(fileName);
        log.info("保存文件到: {}", filePath);

        // 使用InputStream方式保存文件，避免transferTo的路径问题
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath);
        }

        log.info("文件保存成功: {}", filePath);

        // 读取图片并转为base64
        byte[] fileContent = Files.readAllBytes(filePath);
        String base64 = Base64.getEncoder().encodeToString(fileContent);
        
        // OCR识别
        return aiService.ocrImage(base64);
    }

    /**
     * 清理文件名，移除特殊字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        // 移除路径分隔符和特殊字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 诊断解题错误 - 新流程：先判断正确性，再决定是否深入诊断
     */
    public ErrorRecord diagnose(String userId, String questionText, String userAnswer,
                                 String questionImage, String answerImage) {
        // 第一步：判断解答是否正确
        Map<String, Object> correctnessCheck = aiService.checkAnswerCorrectness(questionText, userAnswer);
        boolean isCorrect = (boolean) correctnessCheck.getOrDefault("isCorrect", false);
        String correctAnswer = (String) correctnessCheck.getOrDefault("correctAnswer", "暂无");
        String analysis = (String) correctnessCheck.getOrDefault("analysis", "暂无分析");

        DiagnosisResult result;

        if (isCorrect) {
            // 解答正确，返回正确的反馈
            result = DiagnosisResult.builder()
                .errorType("解答正确")
                .knowledgePoints("已掌握")
                .feedback("恭喜！你的解答完全正确。" + analysis)
                .hintQuestion("")
                .similarExample("暂无")
                .fullSolution(correctAnswer)
                .build();
        } else {
            // 解答不正确，进行详细的错题诊断
            result = aiService.diagnoseSolution(questionText, userAnswer);
            // 使用AI判断的正确答案覆盖诊断结果中的完整解答
            if (!"暂无".equals(correctAnswer) && !"无法获取".equals(correctAnswer)) {
                result.setFullSolution(correctAnswer);
            }
        }

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

        // 更新用户错误档案（只有解答不正确且不是未知类型时才更新）
        if (!isCorrect && !"无错误".equals(result.getErrorType()) && !"未知".equals(result.getErrorType()) && !"解答正确".equals(result.getErrorType())) {
            updateErrorProfile(userId, result.getKnowledgePoints(), result.getErrorType(), result.getFeedback());
        }

        return savedRecord;
    }

    /**
     * 更新用户错误档案
     */
    private void updateErrorProfile(String userId, String knowledgePoints, String errorType, String errorDetails) {
        Optional<UserErrorProfile> existing = userErrorProfileRepository
            .findByUserIdAndKnowledgePointAndErrorType(userId, knowledgePoints, errorType);

        if (existing.isPresent()) {
            UserErrorProfile profile = existing.get();
            profile.setErrorCount(profile.getErrorCount() + 1);
            // 更新错误详情（取最新的反馈内容）
            if (errorDetails != null && !errorDetails.isEmpty()) {
                profile.setErrorDetails(errorDetails);
            }
            userErrorProfileRepository.save(profile);
        } else {
            UserErrorProfile newProfile = UserErrorProfile.builder()
                .userId(userId)
                .knowledgePoint(knowledgePoints)
                .errorType(errorType)
                .errorCount(1)
                .errorDetails(errorDetails)
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

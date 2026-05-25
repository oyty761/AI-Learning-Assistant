package com.example.app.service;

import com.example.app.ai.AiService;
import com.example.app.entity.QaHistory;
import com.example.app.repository.QaHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {

    private final AiService aiService;
    private final QaHistoryRepository qaHistoryRepository;

    /**
     * 苏格拉底式问答
     */
    public String askQuestion(String userId, String question) {
        // 获取最近的历史对话
        List<QaHistory> historyList = qaHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        List<String> history = new ArrayList<>();
        
        for (int i = historyList.size() - 1; i >= 0; i--) {
            QaHistory h = historyList.get(i);
            history.add("Q: " + h.getQuestion());
            history.add("A: " + h.getAnswer());
        }

        // 调用AI服务
        String answer = aiService.socraticDialogue(question, history);

        // 保存问答历史
        QaHistory qaHistory = QaHistory.builder()
            .userId(userId)
            .question(question)
            .answer(answer)
            .build();
        qaHistoryRepository.save(qaHistory);

        return answer;
    }

    /**
     * 获取问答历史
     */
    public List<QaHistory> getHistory(String userId) {
        return qaHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

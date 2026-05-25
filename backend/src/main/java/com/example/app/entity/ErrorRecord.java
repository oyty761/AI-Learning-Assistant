package com.example.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "question_image")
    private String questionImage;
    
    @Column(name = "answer_image")
    private String answerImage;
    
    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;
    
    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;
    
    @Column(name = "error_type")
    private String errorType;
    
    @Column(name = "knowledge_points", columnDefinition = "TEXT")
    private String knowledgePoints;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "hint_question", columnDefinition = "TEXT")
    private String hintQuestion;
    
    @Column(name = "similar_example", columnDefinition = "TEXT")
    private String similarExample;
    
    @Column(name = "full_solution", columnDefinition = "TEXT")
    private String fullSolution;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

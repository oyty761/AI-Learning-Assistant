package com.example.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisResult {
    private String errorType;
    private String knowledgePoints;
    private String feedback;
    private String hintQuestion;
    private String similarExample;
    private String fullSolution;
}

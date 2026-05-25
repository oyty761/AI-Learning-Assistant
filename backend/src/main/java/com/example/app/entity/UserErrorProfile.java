package com.example.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_error_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserErrorProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "knowledge_point", nullable = false)
    private String knowledgePoint;
    
    @Column(name = "error_type", nullable = false)
    private String errorType;
    
    @Column(name = "error_count")
    private Integer errorCount;
    
    @Column(name = "last_error_time")
    private LocalDateTime lastErrorTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (errorCount == null) {
            errorCount = 1;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastErrorTime = LocalDateTime.now();
    }
}

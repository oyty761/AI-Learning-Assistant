package com.example.app.repository;

import com.example.app.entity.UserErrorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserErrorProfileRepository extends JpaRepository<UserErrorProfile, Long> {
    
    List<UserErrorProfile> findByUserId(String userId);
    
    Optional<UserErrorProfile> findByUserIdAndKnowledgePointAndErrorType(
            String userId, String knowledgePoint, String errorType);
    
    List<UserErrorProfile> findByUserIdOrderByErrorCountDesc(String userId);
}

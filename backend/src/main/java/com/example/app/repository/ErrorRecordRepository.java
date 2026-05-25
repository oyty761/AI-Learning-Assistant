package com.example.app.repository;

import com.example.app.entity.ErrorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorRecordRepository extends JpaRepository<ErrorRecord, Long> {
    
    List<ErrorRecord> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<ErrorRecord> findTop5ByUserIdOrderByCreatedAtDesc(String userId);
}

package com.example.app.repository;

import com.example.app.entity.QaHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QaHistoryRepository extends JpaRepository<QaHistory, Long> {
    
    List<QaHistory> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<QaHistory> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}

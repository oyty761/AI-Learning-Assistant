package com.example.app.repository;

import com.example.app.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    List<Note> findByUserIdOrderByCreatedAtDesc(String userId);
    
    long countByUserId(String userId);
    
    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime createdAt);
    
    List<Note> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime createdAt);
}

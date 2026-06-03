package com.example.app.repository;

import com.example.app.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Todo> findByUserIdAndCompletedOrderByCreatedAtDesc(String userId, Boolean completed);

    long countByUserId(String userId);

    long countByUserIdAndCompleted(String userId, Boolean completed);
}

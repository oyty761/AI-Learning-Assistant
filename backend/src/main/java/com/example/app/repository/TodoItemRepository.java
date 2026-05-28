package com.example.app.repository;

import com.example.app.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 待办事项数据访问层
 */
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    /**
     * 根据用户ID查询所有待办事项
     */
    List<TodoItem> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 根据用户ID和完成状态查询待办事项
     */
    List<TodoItem> findByUserIdAndIsCompletedOrderByDueDateAsc(String userId, Boolean isCompleted);

    /**
     * 根据用户ID和分类查询待办事项
     */
    List<TodoItem> findByUserIdAndCategoryOrderByCreatedAtDesc(String userId, String category);

    /**
     * 统计用户的待办事项总数
     */
    long countByUserId(String userId);

    /**
     * 统计用户已完成的待办事项数量
     */
    long countByUserIdAndIsCompleted(String userId, Boolean isCompleted);

    /**
     * 查询用户未完成的待办事项（限制数量）
     */
    List<TodoItem> findTop3ByUserIdAndIsCompletedOrderByDueDateAsc(String userId, Boolean isCompleted);

    /**
     * 查询用户未完成的待办事项（用于首页卡片）
     */
    @Query("SELECT t FROM TodoItem t WHERE t.userId = :userId AND t.isCompleted = false ORDER BY t.dueDate ASC NULLS LAST, t.createdAt DESC")
    List<TodoItem> findPendingTodosByUserId(@Param("userId") String userId);
}

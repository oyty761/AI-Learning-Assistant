package com.example.app.service;

import com.example.app.entity.TodoItem;
import com.example.app.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待办事项服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoItemRepository todoItemRepository;

    /**
     * 创建待办事项
     */
    @Transactional
    public TodoItem createTodo(TodoItem todoItem) {
        log.info("创建待办事项: {}", todoItem.getTitle());
        return todoItemRepository.save(todoItem);
    }

    /**
     * 更新待办事项
     */
    @Transactional
    public TodoItem updateTodo(Long id, TodoItem todoItem) {
        TodoItem existingTodo = todoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("待办事项不存在"));

        existingTodo.setTitle(todoItem.getTitle());
        existingTodo.setDescription(todoItem.getDescription());
        existingTodo.setDueDate(todoItem.getDueDate());
        existingTodo.setCategory(todoItem.getCategory());
        existingTodo.setPriority(todoItem.getPriority());

        log.info("更新待办事项: {}", id);
        return todoItemRepository.save(existingTodo);
    }

    /**
     * 删除待办事项
     */
    @Transactional
    public void deleteTodo(Long id) {
        log.info("删除待办事项: {}", id);
        todoItemRepository.deleteById(id);
    }

    /**
     * 切换待办事项完成状态
     */
    @Transactional
    public TodoItem toggleTodoStatus(Long id) {
        TodoItem todo = todoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("待办事项不存在"));

        boolean newStatus = !todo.getIsCompleted();
        todo.setIsCompleted(newStatus);

        if (newStatus) {
            todo.setCompletedAt(LocalDateTime.now());
        } else {
            todo.setCompletedAt(null);
        }

        log.info("切换待办事项状态: {}, 新状态: {}", id, newStatus);
        return todoItemRepository.save(todo);
    }

    /**
     * 根据ID获取待办事项
     */
    public TodoItem getTodoById(Long id) {
        return todoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("待办事项不存在"));
    }

    /**
     * 获取用户的所有待办事项
     */
    public List<TodoItem> getTodosByUserId(String userId) {
        return todoItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 根据完成状态筛选待办事项
     */
    public List<TodoItem> getTodosByStatus(String userId, Boolean isCompleted) {
        return todoItemRepository.findByUserIdAndIsCompletedOrderByDueDateAsc(userId, isCompleted);
    }

    /**
     * 根据分类筛选待办事项
     */
    public List<TodoItem> getTodosByCategory(String userId, String category) {
        return todoItemRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
    }

    /**
     * 获取待办事项统计信息（用于首页卡片）
     */
    public Map<String, Object> getTodoStats(String userId) {
        long total = todoItemRepository.countByUserId(userId);
        long completed = todoItemRepository.countByUserIdAndIsCompleted(userId, true);
        long pending = total - completed;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("pending", pending);

        return stats;
    }

    /**
     * 获取待办事项列表（支持排序）
     */
    public List<TodoItem> getTodosWithSort(String userId, String sortBy) {
        List<TodoItem> todos = todoItemRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if ("dueDate".equals(sortBy)) {
            todos.sort((a, b) -> {
                if (a.getDueDate() == null && b.getDueDate() == null) return 0;
                if (a.getDueDate() == null) return 1;
                if (b.getDueDate() == null) return -1;
                return a.getDueDate().compareTo(b.getDueDate());
            });
        } else if ("priority".equals(sortBy)) {
            todos.sort((a, b) -> {
                int priorityA = a.getPriority() != null ? a.getPriority() : 1;
                int priorityB = b.getPriority() != null ? b.getPriority() : 1;
                return Integer.compare(priorityB, priorityA);
            });
        }

        return todos;
    }
}

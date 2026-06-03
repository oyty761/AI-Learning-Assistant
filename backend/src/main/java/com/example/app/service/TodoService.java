package com.example.app.service;

import com.example.app.entity.Todo;
import com.example.app.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public Todo createTodo(String userId, String title, String description, Integer priority) {
        Todo todo = Todo.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .completed(false)
                .priority(priority != null ? priority : 1)
                .build();
        return todoRepository.save(todo);
    }

    public List<Todo> getTodosByUserId(String userId) {
        return todoRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Todo> getTodosByUserIdAndStatus(String userId, Boolean completed) {
        return todoRepository.findByUserIdAndCompletedOrderByCreatedAtDesc(userId, completed);
    }

    public Todo updateTodo(Long id, String userId, String title, String description, Integer priority) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("待办事项不存在"));

        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此待办事项");
        }

        if (title != null) {
            todo.setTitle(title);
        }
        if (description != null) {
            todo.setDescription(description);
        }
        if (priority != null) {
            todo.setPriority(priority);
        }

        return todoRepository.save(todo);
    }

    public Todo toggleTodoStatus(Long id, String userId) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("待办事项不存在"));

        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此待办事项");
        }

        todo.setCompleted(!todo.getCompleted());
        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id, String userId) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("待办事项不存在"));

        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此待办事项");
        }

        todoRepository.delete(todo);
    }

    public Map<String, Object> getTodoStats(String userId) {
        long total = todoRepository.countByUserId(userId);
        long completed = todoRepository.countByUserIdAndCompleted(userId, true);

        return Map.of(
                "total", total,
                "completed", completed,
                "pending", total - completed
        );
    }
}

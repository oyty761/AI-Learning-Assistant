package com.example.app.controller;

import com.example.app.entity.TodoItem;
import com.example.app.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 待办事项控制器
 */
@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TodoController {

    private final TodoService todoService;

    /**
     * 创建待办事项
     */
    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody TodoItem todoItem) {
        try {
            TodoItem created = todoService.createTodo(todoItem);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新待办事项
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable Long id, @RequestBody TodoItem todoItem) {
        try {
            TodoItem updated = todoService.updateTodo(id, todoItem);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除待办事项
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        try {
            todoService.deleteTodo(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }

    /**
     * 切换待办事项完成状态
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTodoStatus(@PathVariable Long id) {
        try {
            TodoItem updated = todoService.toggleTodoStatus(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("状态切换失败: " + e.getMessage());
        }
    }

    /**
     * 获取待办事项详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTodoById(@PathVariable Long id) {
        try {
            TodoItem todo = todoService.getTodoById(id);
            return ResponseEntity.ok(todo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有待办事项
     */
    @GetMapping("/list")
    public ResponseEntity<?> getTodosByUserId(@RequestParam String userId) {
        try {
            List<TodoItem> todos = todoService.getTodosByUserId(userId);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据完成状态筛选待办事项
     */
    @GetMapping("/list/by-status")
    public ResponseEntity<?> getTodosByStatus(
            @RequestParam String userId,
            @RequestParam Boolean isCompleted) {
        try {
            List<TodoItem> todos = todoService.getTodosByStatus(userId, isCompleted);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类筛选待办事项
     */
    @GetMapping("/list/by-category")
    public ResponseEntity<?> getTodosByCategory(
            @RequestParam String userId,
            @RequestParam String category) {
        try {
            List<TodoItem> todos = todoService.getTodosByCategory(userId, category);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取待办事项统计信息（用于首页卡片）
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTodoStats(@RequestParam String userId) {
        try {
            Map<String, Object> stats = todoService.getTodoStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取待办事项列表（支持排序）
     */
    @GetMapping("/list/sorted")
    public ResponseEntity<?> getTodosWithSort(
            @RequestParam String userId,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        try {
            List<TodoItem> todos = todoService.getTodosWithSort(userId, sortBy);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取失败: " + e.getMessage());
        }
    }
}

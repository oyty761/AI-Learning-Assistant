package com.example.app.controller;

import com.example.app.entity.Todo;
import com.example.app.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            Integer priority = request.get("priority") != null ?
                    ((Number) request.get("priority")).intValue() : 1;

            if (userId == null || title == null) {
                return ResponseEntity.badRequest().body("缺少必要参数");
            }

            Todo todo = todoService.createTodo(userId, title, description, priority);
            return ResponseEntity.ok(todo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("创建失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getTodos(@RequestParam String userId,
                                      @RequestParam(required = false) Boolean completed) {
        try {
            List<Todo> todos;
            if (completed != null) {
                todos = todoService.getTodosByUserIdAndStatus(userId, completed);
            } else {
                todos = todoService.getTodosByUserId(userId);
            }
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getTodoStats(@RequestParam String userId) {
        try {
            Map<String, Object> stats = todoService.getTodoStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("获取统计失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            Integer priority = request.get("priority") != null ?
                    ((Number) request.get("priority")).intValue() : null;

            if (userId == null) {
                return ResponseEntity.badRequest().body("缺少用户ID");
            }

            Todo todo = todoService.updateTodo(id, userId, title, description, priority);
            return ResponseEntity.ok(todo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleTodoStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body("缺少用户ID");
            }

            Todo todo = todoService.toggleTodoStatus(id, userId);
            return ResponseEntity.ok(todo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("切换状态失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id, @RequestParam String userId) {
        try {
            todoService.deleteTodo(id, userId);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }
}

package com.example.app.controller;

import com.example.app.entity.Note;
import com.example.app.service.NoteService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NoteController {

    private final NoteService noteService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> result = noteService.uploadAndExtractText(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件上传失败");
            error.put("message", e.getMessage());
            error.put("text", "文件上传失败，请重试或手动输入内容");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件处理失败");
            error.put("message", e.getMessage());
            error.put("text", "文件处理失败，请重试或手动输入内容");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 生成结构化笔记
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateNotes(@RequestBody GenerateNotesRequest request) {
        try {
            Note note = noteService.generateNotes(
                request.getUserId(),
                request.getTheme(),
                request.getConcepts(),
                request.getContent(),
                request.getSourceFile()
            );
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成笔记失败: " + e.getMessage());
        }
    }

    /**
     * 生成笔记请求DTO
     */
    @Data
    public static class GenerateNotesRequest {
        private String userId;
        private String theme;
        private String concepts;
        private String content;
        private String sourceFile;
    }

    /**
     * 获取用户的所有笔记
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserNotes(@RequestParam String userId) {
        List<Note> notes = noteService.getUserNotes(userId);
        return ResponseEntity.ok(notes);
    }

    /**
     * 获取单条笔记
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoteById(@PathVariable Long id) {
        Note note = noteService.getNoteById(id);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(note);
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok("删除成功");
    }
}

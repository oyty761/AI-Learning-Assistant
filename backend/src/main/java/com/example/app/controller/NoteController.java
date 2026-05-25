package com.example.app.controller;

import com.example.app.entity.Note;
import com.example.app.service.NoteService;
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
            String fileName = noteService.uploadFile(file);
            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("message", "文件上传成功");
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 生成结构化笔记
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateNotes(
            @RequestParam String userId,
            @RequestParam String theme,
            @RequestParam String concepts,
            @RequestParam String content,
            @RequestParam(required = false) String sourceFile) {
        
        try {
            Note note = noteService.generateNotes(userId, theme, concepts, content, sourceFile);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成笔记失败: " + e.getMessage());
        }
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

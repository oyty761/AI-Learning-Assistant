package com.example.app.service;

import com.example.app.ai.AiService;
import com.example.app.entity.Note;
import com.example.app.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final AiService aiService;

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, fileName);
        Files.createDirectories(filePath.getParent());
        file.transferTo(filePath.toFile());
        return fileName;
    }

    /**
     * 生成结构化笔记
     */
    public Note generateNotes(String userId, String theme, String concepts, String content, String sourceFile) {
        String generatedContent = aiService.generateNotes(theme, concepts, content);
        
        Note note = Note.builder()
            .userId(userId)
            .title(theme)
            .theme(theme)
            .concepts(concepts)
            .content(generatedContent)
            .sourceFile(sourceFile)
            .build();
        
        return noteRepository.save(note);
    }

    /**
     * 获取用户的所有笔记
     */
    public List<Note> getUserNotes(String userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取单条笔记
     */
    public Note getNoteById(Long id) {
        return noteRepository.findById(id).orElse(null);
    }

    /**
     * 删除笔记
     */
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
}

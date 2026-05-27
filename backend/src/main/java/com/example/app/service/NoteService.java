package com.example.app.service;

import com.example.app.ai.AiService;
import com.example.app.entity.Note;
import com.example.app.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 上传文件并提取文本内容（支持PDF、图片OCR）
     */
    public Map<String, String> uploadAndExtractText(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + sanitizeFileName(originalFileName);

        // 使用绝对路径确保目录存在
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(fileName);
        log.info("保存文件到: {}", filePath);

        // 使用InputStream方式保存文件，避免transferTo的路径问题
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath);
        }

        log.info("文件保存成功: {}", filePath);

        Map<String, String> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("originalFileName", originalFileName);

        // 根据文件类型提取文本
        String fileExtension = getFileExtension(originalFileName);

        String extractedText = "";
        try {
            if (fileExtension.equals("pdf")) {
                extractedText = extractTextFromPdf(filePath.toFile());
            } else if (isImageFile(fileExtension)) {
                // 图片文件使用OCR识别
                byte[] fileContent = Files.readAllBytes(filePath);
                String base64 = java.util.Base64.getEncoder().encodeToString(fileContent);
                extractedText = aiService.ocrImage(base64);
            } else if (fileExtension.equals("ppt")) {
                extractedText = extractTextFromPpt(filePath.toFile());
            } else if (fileExtension.equals("pptx")) {
                extractedText = extractTextFromPptx(filePath.toFile());
            } else {
                extractedText = "不支持的文件格式，请手动输入内容";
            }
        } catch (Exception e) {
            log.error("提取文件内容失败", e);
            extractedText = "文件内容提取失败: " + e.getMessage() + "，请手动输入内容";
        }

        result.put("text", extractedText);
        result.put("message", "文件上传成功");
        return result;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 清理文件名，移除特殊字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        // 移除路径分隔符和特殊字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") ||
               extension.equals("bmp") || extension.equals("webp");
    }

    /**
     * 从PDF文件中提取文本（使用Apache PDFBox）
     */
    private String extractTextFromPdf(File pdfFile) {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text != null && !text.trim().isEmpty()) {
                // 限制文本长度，避免过长
                if (text.length() > 10000) {
                    text = text.substring(0, 10000) + "\n\n[内容过长，已截断]";
                }
                return text.trim();
            } else {
                return "PDF文件中没有提取到文本内容，可能是扫描版PDF，请手动输入。";
            }
        } catch (Exception e) {
            log.error("PDF文本提取失败", e);
            return "PDF内容提取失败: " + e.getMessage() + "，请手动输入内容";
        }
    }

    /**
     * 从PPT文件(.ppt)中提取文本（使用Apache POI HSLF）
     */
    private String extractTextFromPpt(File pptFile) {
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(pptFile);
             HSLFSlideShow slideshow = new HSLFSlideShow(fis)) {

            List<HSLFSlide> slides = slideshow.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                HSLFSlide slide = slides.get(i);
                text.append("\n=== 第 ").append(i + 1).append(" 页 ===\n");

                for (HSLFShape shape : slide.getShapes()) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        String shapeText = textShape.getText();
                        if (shapeText != null && !shapeText.trim().isEmpty()) {
                            text.append(shapeText).append("\n");
                        }
                    }
                }
            }

            String result = text.toString().trim();
            if (!result.isEmpty()) {
                // 限制文本长度
                if (result.length() > 10000) {
                    result = result.substring(0, 10000) + "\n\n[内容过长，已截断]";
                }
                return result;
            } else {
                return "PPT文件中没有提取到文本内容，请手动输入。";
            }
        } catch (Exception e) {
            log.error("PPT文本提取失败", e);
            return "PPT内容提取失败: " + e.getMessage() + "，请手动输入内容";
        }
    }

    /**
     * 从PPTX文件(.pptx)中提取文本（使用Apache POI XSLF）
     */
    private String extractTextFromPptx(File pptxFile) {
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(pptxFile);
             XMLSlideShow slideshow = new XMLSlideShow(fis)) {

            List<XSLFSlide> slides = slideshow.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                XSLFSlide slide = slides.get(i);
                text.append("\n=== 第 ").append(i + 1).append(" 页 ===\n");

                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String shapeText = textShape.getText();
                        if (shapeText != null && !shapeText.trim().isEmpty()) {
                            text.append(shapeText).append("\n");
                        }
                    }
                }
            }

            String result = text.toString().trim();
            if (!result.isEmpty()) {
                // 限制文本长度
                if (result.length() > 10000) {
                    result = result.substring(0, 10000) + "\n\n[内容过长，已截断]";
                }
                return result;
            } else {
                return "PPTX文件中没有提取到文本内容，请手动输入。";
            }
        } catch (Exception e) {
            log.error("PPTX文本提取失败", e);
            return "PPTX内容提取失败: " + e.getMessage() + "，请手动输入内容";
        }
    }

    /**
     * 生成结构化笔记
     */
    public Note generateNotes(String userId, String theme, String concepts, String content, String sourceFile) {
        String generatedContent = aiService.generateNotes(theme, concepts, content);
        
        // 如果用户没有输入标题，从AI生成的内容中提取标题（第一行#开头的标题）
        String extractedTitle = theme;
        if (extractedTitle == null || extractedTitle.trim().isEmpty()) {
            extractedTitle = extractTitleFromContent(generatedContent);
        }
        
        Note note = Note.builder()
            .userId(userId)
            .title(extractedTitle)
            .theme(theme)
            .concepts(concepts)
            .content(generatedContent)
            .sourceFile(sourceFile)
            .build();
        
        return noteRepository.save(note);
    }
    
    /**
     * 从生成的笔记内容中提取标题
     */
    private String extractTitleFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "未命名笔记";
        }
        
        // 按行分割
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            // 查找Markdown标题格式 # 标题
            if (line.startsWith("# ")) {
                return line.substring(2).trim();
            }
        }
        
        // 如果没有找到#开头的标题，返回前20个字符
        String firstLine = lines[0].trim();
        if (firstLine.length() > 20) {
            return firstLine.substring(0, 20) + "...";
        }
        return firstLine.isEmpty() ? "未命名笔记" : firstLine;
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

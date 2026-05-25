# "人工智能的数学思维"课程学习助手 - 实现计划

## 1. 项目概述

基于"识别个人学习瓶颈-动态介入-闭环反馈"的学习闭环理念，构建一个模块化的智能学习助手。

### 核心模块
| 模块 | 功能描述 |
|------|----------|
| 笔记整理官 | PPT/PDF解析，生成结构化笔记 |
| 对话导师 | 苏格拉底式问答，概念串联 |
| 解题诊断师 | 错误归因与针对性反馈 |
| 出题教练 | 个性化练习生成 |

## 2. 技术方案

### 2.1 技术栈选择

| 层次 | 技术 | 选型理由 |
|------|------|----------|
| 前端 | Vue.js 3 + Element Plus | 简洁大方，生态成熟，学习曲线平缓 |
| 后端 | Java Spring Boot 3.x | 社区成熟，便于文件处理和业务路由 |
| 数据库 | MySQL 8.x | 稳定可靠，社区支持好 |
| AI集成 | 百度文心一言 API | 国内访问稳定，支持多模态 |
| OCR | 百度OCR API | 中文识别效果好 |
| 容器化 | Docker Compose | 本地一键部署 |

### 2.2 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                     前端 (Vue.js)                          │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐ ┌────────────┐   │
│  │笔记整理官│ │对话导师  │ │解题诊断师  │ │出题教练    │   │
│  └────┬─────┘ └────┬─────┘ └─────┬──────┘ └─────┬──────┘   │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
┌───────▼─────────────▼─────────────▼─────────────▼──────────┐
│                     后端 API Gateway                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  /api/notes     /api/tutor    /api/diagnose    /api/exam│ │
│  └─────────────────────────────────────────────────────┘   │
└───────┬─────────────┬─────────────┬─────────────┬──────────┘
        │             │             │             │
┌───────▼───────┐ ┌───▼───────┐ ┌───▼───────┐ ┌─▼─────────┐
│ 笔记服务     │ │导师服务   │ │诊断服务   │ │出题服务   │
└───────┬───────┘ └────┬──────┘ └────┬──────┘ └────┬──────┘
        │              │             │             │
        └──────────────┼─────────────┼─────────────┘
                      ▼
           ┌─────────────────────┐
           │   AI Service Layer  │
           │  (统一调用大模型API) │
           └─────────┬───────────┘
                     ▼
           ┌─────────────────────┐
           │     数据库          │
           │  MySQL 8.x         │
           └─────────────────────┘
```

## 3. 数据库设计

### 3.1 用户错误档案表 (user_error_profile)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| user_id | VARCHAR(64) | 用户标识 |
| knowledge_point | VARCHAR(128) | 知识点标签 |
| error_type | VARCHAR(32) | 错误类型（概念误解/计算疏忽/迁移困难/逻辑断层） |
| error_count | INT | 错误频次 |
| last_error_time | DATETIME | 最近错误时间 |
| created_at | DATETIME | 创建时间 |

### 3.2 问答历史表 (qa_history)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| user_id | VARCHAR(64) | 用户标识 |
| question | TEXT | 用户问题 |
| answer | TEXT | AI回答 |
| knowledge_points | TEXT | 涉及知识点（JSON数组） |
| created_at | DATETIME | 创建时间 |

### 3.3 笔记表 (notes)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| user_id | VARCHAR(64) | 用户标识 |
| title | VARCHAR(256) | 笔记标题 |
| content | LONGTEXT | Markdown内容 |
| source_file | VARCHAR(256) | 源文件名 |
| created_at | DATETIME | 创建时间 |

### 3.4 错题记录表 (error_records)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| user_id | VARCHAR(64) | 用户标识 |
| question_image | VARCHAR(256) | 题目图片路径 |
| answer_image | VARCHAR(256) | 解答图片路径 |
| question_text | TEXT | OCR识别的题目文本 |
| user_answer | TEXT | OCR识别的解答文本 |
| error_type | VARCHAR(32) | 错误类型 |
| knowledge_points | TEXT | 涉及知识点 |
| feedback | TEXT | AI反馈内容 |
| created_at | DATETIME | 创建时间 |

## 4. 后端接口设计

### 4.1 笔记整理模块

| API路径 | 方法 | 功能 |
|---------|------|------|
| /api/notes/upload | POST | 上传PPT/PDF文件 |
| /api/notes/generate | POST | 生成结构化笔记 |
| /api/notes/list | GET | 获取笔记列表 |
| /api/notes/{id} | GET | 获取单条笔记 |
| /api/notes/{id} | DELETE | 删除笔记 |

### 4.2 对话导师模块

| API路径 | 方法 | 功能 |
|---------|------|------|
| /api/tutor/ask | POST | 提问接口 |
| /api/tutor/history | GET | 获取问答历史 |

### 4.3 解题诊断模块

| API路径 | 方法 | 功能 |
|---------|------|------|
| /api/diagnose/upload | POST | 上传题目和解答图片 |
| /api/diagnose/analyze | POST | 分析解答错误 |
| /api/diagnose/records | GET | 获取诊断记录 |

### 4.4 出题教练模块

| API路径 | 方法 | 功能 |
|---------|------|------|
| /api/exam/generate | POST | 生成个性化练习题 |
| /api/exam/list | GET | 获取练习题列表 |
| /api/profile/{userId} | GET | 获取用户学习档案 |

## 5. 前端页面设计

### 5.1 页面结构

| 页面 | 功能 | 组件 |
|------|------|------|
| 首页 | 模块导航 | Header, ModuleCard |
| 笔记整理 | 上传+笔记展示 | FileUploader, NotePreview |
| 对话导师 | 聊天界面 | ChatPanel, MessageItem |
| 解题诊断 | 图片上传+反馈展示 | ImageUploader, DiagnosisResult |
| 出题教练 | 练习展示+统计 | ExamList, ProgressChart |

### 5.2 核心交互流程

**笔记整理流程**：
1. 用户上传文件 → 选择核心主题 → 生成笔记 → 预览/导出/跳转问答

**解题诊断流程**：
1. 上传题目+解答图片 → OCR识别 → AI分析错误 → 展示反馈 → 记录到档案

**学习闭环流程**：
1. 诊断发现薄弱点 → 系统建议练习 → 出题教练生成题目 → 完成练习 → 再次诊断

## 6. AI集成设计

### 6.1 统一AI服务层

```java
// AI服务接口设计
public interface AiService {
    // 笔记整理
    String generateNotes(String theme, String concepts, String content);
    
    // 对话问答
    String chat(String question, List<String> history);
    
    // 解题诊断
    DiagnosisResult analyzeSolution(String question, String userAnswer);
    
    // 出题生成
    String generateExam(String knowledgePoint, String errorType);
}
```

### 6.2 错误类型枚举

```java
public enum ErrorType {
    CONCEPT_MISUNDERSTANDING("概念误解型"),
    CALCULATION_ERROR("计算疏忽型"),
    TRANSFER_DIFFICULTY("迁移困难型"),
    LOGIC_GAP("逻辑断层型");
}
```

## 7. 部署方案

### 7.1 Docker Compose 配置

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: learning_assistant
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/learning_assistant
    depends_on:
      - mysql

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### 7.2 环境变量配置

后端需要配置以下环境变量：
- `BAIDU_API_KEY`: 百度文心API Key
- `BAIDU_SECRET_KEY`: 百度文心Secret Key
- `BAIDU_OCR_API_KEY`: 百度OCR API Key
- `BAIDU_OCR_SECRET_KEY`: 百度OCR Secret Key

## 8. 安全考虑

1. **文件上传限制**：限制文件大小（<10MB），校验文件类型
2. **API认证**：使用简单的API Key认证（本地运行场景）
3. **数据存储**：敏感配置使用环境变量，不硬编码
4. **输入验证**：对所有用户输入进行严格校验

## 9. 实现步骤

| 阶段 | 任务 | 时间估算 |
|------|------|----------|
| 1 | 环境搭建（Docker + MySQL） | 1天 |
| 2 | 后端项目初始化 + 数据库设计 | 2天 |
| 3 | AI服务集成（百度API） | 2天 |
| 4 | 笔记整理模块开发 | 2天 |
| 5 | 对话导师模块开发 | 2天 |
| 6 | 解题诊断模块开发 | 3天 |
| 7 | 出题教练模块开发 | 2天 |
| 8 | 前端页面开发 | 3天 |
| 9 | 测试与调试 | 2天 |

## 10. 关键设计要点

### 10.1 学习闭环实现

```
用户学习 → 发现问题 → 诊断分析 → 针对性练习 → 再次诊断 → 掌握度提升
     ↑                                                    │
     └────────────────────────────────────────────────────┘
```

### 10.2 掌握度评分算法（简化版）

```
掌握度 = (1 - 错误频次/总练习次数) × 100
当掌握度 < 60 → 推荐强化练习
当掌握度 > 80 → 标记为已掌握
```

### 10.3 人工校验机制

对于OCR识别结果，提供编辑界面让用户确认/修正：
- 题目识别后显示原文，允许用户编辑
- 解答识别后显示原文，允许用户编辑
- 确认后再进行AI分析

## 11. 代码组织

### 后端目录结构

```
backend/
├── src/main/java/com/example/app/
│   ├── controller/     # REST API控制层
│   ├── service/        # 业务逻辑层
│   ├── repository/     # 数据访问层
│   ├── entity/         # 数据库实体
│   ├── dto/            # 数据传输对象
│   ├── config/         # 配置类
│   ├── ai/             # AI服务集成
│   └── Application.java
└── src/main/resources/
    └── application.yml
```

### 前端目录结构

```
frontend/
├── src/
│   ├── components/     # 可复用组件
│   ├── views/          # 页面视图
│   ├── api/            # API调用封装
│   ├── store/          # 状态管理
│   └── App.vue
└── package.json
```

---

## 附录：设计盲点检查清单

- [ ] 用户错误档案的数据结构是否支持灵活扩展？
- [ ] 掌握度评分算法是否需要更复杂的加权模型？
- [ ] OCR识别失败时的降级处理机制？
- [ ] 大模型API调用失败时的重试策略？
- [ ] 用户隐私数据的本地存储安全性？
- [ ] 多用户场景的隔离机制？
- [ ] 学习进度的可视化展示？
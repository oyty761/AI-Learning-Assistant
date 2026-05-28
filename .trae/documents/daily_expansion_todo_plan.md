# 智能学习助手修复计划

## 项目结构分析

### 技术栈

* **前端**: Vue 3 + Element Plus + Vite

* **后端**: Spring Boot + H2数据库

* **AI服务**: ChatECNU (华东师大AI平台)

### 功能模块

1. 笔记整理官 (`/notes`) - 上传PPT/PDF生成结构化笔记
2. 对话导师 (`/tutor`) - AI问答对话
3. 解题诊断师 (`/diagnose`) - 上传题目和解答进行诊断
4. 出题教练 (`/exam`) - 生成个性化练习题

***

## 问题清单与修复方案

### 问题1: 问答部分连续对话被分开记录

**问题描述**: 与AI连续对话时，每一行对话会被认为是新的一轮，单独存在对话记录中，而不是在同一个对话会话中。

**根本原因分析**:

* 当前 `QaHistory` 表结构只有 `userId`, `question`, `answer`，没有会话(session)概念

* 每次提问都创建一条独立记录，没有将同一轮对话关联起来

* 前端 `Tutor.vue` 中的 `messages` 数组在页面刷新后会清空

**修复方案**:

1. **数据库层面**: 添加 `session_id` 字段到 `QaHistory` 表，用于标识同一会话
2. **后端层面**:

   * 修改 `TutorService` 支持会话管理

   * 添加获取特定会话历史记录的接口

   * 添加创建新会话接口
3. **前端层面**:

   * 添加当前会话ID状态管理

   * 支持新建对话按钮

   * 历史记录列表按会话分组显示

   * 点击历史记录加载整个会话的对话

**具体修改文件**:

* `backend/src/main/java/com/example/app/entity/QaHistory.java`

* `backend/src/main/java/com/example/app/service/TutorService.java`

* `backend/src/main/java/com/example/app/controller/TutorController.java`

* `frontend/src/views/Tutor.vue`

* `frontend/src/api/index.js`

***

### 问题2: AI回答AI味太浓，语句假大空

**问题描述**: AI回答过于官方、生硬，不易于理解。

**根本原因分析**:

* `AiService.java` 中的提示词(prompt)设计过于正式

* 没有要求AI使用通俗易懂的语言

* 缺少对AI persona的具体定义

**修复方案**:
修改 `AiService.java` 中的提示词，要求AI:

1. 使用通俗易懂的语言，像朋友一样交流
2. 避免使用过于官方或学术化的套话
3. 多用具体例子说明抽象概念
4. 适当使用emoji增加亲和力
5. 回答结构清晰，分点说明

**具体修改文件**:

* `backend/src/main/java/com/example/app/ai/AiService.java`

***

### 问题3: 历史对话无法加载

**问题描述**: 问答历史只能显示问题列表，点击后无法加载完整对话。

**根本原因分析**:

* `Tutor.vue` 中的 `loadHistoryItem` 方法只是显示"功能开发中"

* 后端没有提供获取单条历史记录完整对话的接口

* 数据库设计没有支持多轮对话存储

**修复方案**:

1. 配合问题1的会话机制修复
2. 添加获取会话完整对话历史的API
3. 前端实现点击历史记录加载完整对话功能

**具体修改文件**:

* `backend/src/main/java/com/example/app/controller/TutorController.java`

* `frontend/src/views/Tutor.vue`

***

### 问题4: 错误分析功能文件与图片无法上传

**问题描述**: 解题诊断页面图片上传失败。

**根本原因分析**:

* `DiagnoseController.java` 中 `uploadAndOcr` 方法调用 `aiService.ocrImage()`

* `AiService.java` 中 `ocrEnabled = false`，OCR功能被禁用

* 上传接口返回空字符串，前端无法正确处理

**修复方案**:

1. 实现OCR功能，或至少让上传接口正常工作
2. 可以接入第三方OCR服务（如百度OCR、腾讯OCR）
3. 或者先实现文件上传保存，OCR功能后续完善
4. 前端添加上传进度和错误处理

**具体修改文件**:

* `backend/src/main/java/com/example/app/ai/AiService.java`

* `backend/src/main/java/com/example/app/service/DiagnoseService.java`

* `backend/src/main/java/com/example/app/controller/DiagnoseController.java`

* `frontend/src/views/Diagnose.vue`

***

### 问题5: 解题诊断功能无输出

**问题描述**: 输入题目和错误解答后，诊断结果为空。

**根本原因分析**:

* 从截图看，诊断结果显示"未知"错误类型

* `AiService.diagnoseSolution()` 方法中AI返回的结果无法正确解析为JSON

* 可能是AI返回格式不符合预期，或AI服务调用失败

**修复方案**:

1. 优化提示词，确保AI返回标准JSON格式
2. 添加更健壮的JSON解析逻辑
3. 添加日志记录，便于调试
4. 当解析失败时，返回更友好的错误信息

**具体修改文件**:

* `backend/src/main/java/com/example/app/ai/AiService.java`

***

### 问题6: 四个功能模块命名不能望名知意

**问题描述**: 当前命名"笔记整理官"、"对话导师"、"解题诊断师"、"出题教练"不够直观。

**修复方案**:
改为更直观的功能名称:

* "笔记整理官" → "智能笔记" 或 "笔记生成"

* "对话导师" → "AI问答" 或 "智能问答"

* "解题诊断师" → "错题诊断" 或 "解题分析"

* "出题教练" → "智能练习" 或 "个性出题"

**具体修改文件**:

* `frontend/src/App.vue` (导航菜单)

* `frontend/src/views/Home.vue` (首页模块卡片)

* `frontend/src/views/Notes.vue` (页面标题)

* `frontend/src/views/Tutor.vue` (页面标题)

* `frontend/src/views/Diagnose.vue` (页面标题)

* `frontend/src/views/Exam.vue` (页面标题)

***

### 问题7: 导航栏菜单设计优化

**问题描述**: 从截图看，最后一个功能"出题教练"被藏在省略号里。

**根本原因分析**:

* Element Plus的`el-menu`在水平模式下，当空间不足时会折叠

* 当前5个菜单项（首页、笔记整理、对话导师、解题诊断、出题教练）可能超出容器宽度

**修复方案**:

1. 调整导航栏样式，确保所有菜单项可见
2. 或者减少菜单项数量（首页可以合并或移除）
3. 使用响应式设计，在小屏幕上使用汉堡菜单

**具体修改文件**:

* `frontend/src/App.vue`

***

### 问题8: OCR图像识别功能暂不支持

**问题描述**: 图片上传后无法识别文字。

**修复方案**:

1. 接入第三方OCR API（如百度智能云OCR、腾讯云OCR、阿里云OCR）
2. 或使用开源OCR方案（如PaddleOCR、Tesseract）
3. 在配置文件中添加OCR服务配置
4. 前端添加OCR识别状态提示

**具体修改文件**:

* `backend/src/main/java/com/example/app/ai/AiService.java`

* `backend/src/main/resources/application.yml`

* `frontend/src/views/Diagnose.vue`

* `frontend/src/views/Notes.vue`

***

## 实施顺序建议

按照优先级和依赖关系，建议按以下顺序实施:

### 第一阶段（高优先级）

1. **问题2** - 优化AI提示词（改动小，效果明显）
2. **问题5** - 修复诊断功能无输出（核心功能）
3. **问题4** - 修复上传功能（核心功能）

### 第二阶段（中优先级）

1. **问题1+3** - 实现对话会话机制（需要数据库变更）
2. **问题6** - 重命名功能模块（简单文本修改）
3. **问题7** - 优化导航栏（UI调整）

### 第三阶段（低优先级）

1. **问题8** - 实现OCR功能（需要接入第三方服务）

***

## 技术实现细节

### 数据库变更

```sql
-- 添加会话支持
ALTER TABLE qa_history ADD COLUMN session_id VARCHAR(64);
ALTER TABLE qa_history ADD COLUMN session_title VARCHAR(255);
CREATE INDEX idx_session_id ON qa_history(session_id);
```

### API变更

新增接口:

* `POST /api/tutor/session` - 创建新会话

* `GET /api/tutor/sessions?userId={userId}` - 获取用户所有会话列表

* `GET /api/tutor/session/{sessionId}/messages` - 获取会话完整对话

修改接口:

* `POST /api/tutor/ask` - 添加sessionId参数

### 前端状态管理

在 `Tutor.vue` 中添加:

```javascript
const currentSessionId = ref(null)
const sessions = ref([]) // 会话列表
```


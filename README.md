# AI数学思维学习助手

基于"识别个人学习瓶颈-动态介入-闭环反馈"理念构建的智能学习系统。

## 系统架构

### 核心模块

1. **笔记整理官** - PPT/PDF解析，生成结构化笔记
2. **对话导师** - 苏格拉底式问答，深入理解概念
3. **解题诊断师** - 错误归因与针对性反馈
4. **出题教练** - 个性化练习生成

### 技术栈

- **前端**: Vue.js 3 + Element Plus + Vite
- **后端**: Java Spring Boot 3.x + H2数据库（无需安装MySQL）
- **AI服务**: 百度文心一言 API + 百度OCR

## 环境要求

- **Java**: JDK 17 或更高版本
- **Node.js**: 18 或更高版本
- **Maven**: 3.6 或更高版本（后端构建）

## 快速开始（本地运行）

### 1. 配置百度AI API密钥

编辑 `backend/src/main/resources/application.yml`，填入你的百度API密钥：

```yaml
ai:
  baidu:
    api-key: 你的API_KEY
    secret-key: 你的SECRET_KEY
    ocr-api-key: 你的OCR_API_KEY
    ocr-secret-key: 你的OCR_SECRET_KEY
```

> 获取方式：访问 [百度智能云](https://cloud.baidu.com/) 创建应用获取API密钥

### 2. 启动后端服务

```bash
# 进入后端目录
cd backend

# 编译并运行（第一次需要下载依赖，会比较慢）
./mvnw spring-boot:run

# Windows使用
mvnw.cmd spring-boot:run
```

后端服务将启动在 http://localhost:8080

### 3. 启动前端服务

打开**新的终端窗口**：

```bash
# 进入前端目录
cd frontend

# 安装依赖（第一次需要）
npm install

# 启动开发服务器
npm run dev
```

前端服务将启动在 http://localhost:3000

### 4. 访问应用

打开浏览器访问 http://localhost:3000 即可使用！

## 功能特性

### 学习闭环

```
用户学习 → 发现问题 → 诊断分析 → 针对性练习 → 再次诊断 → 掌握度提升
     ↑                                                    │
     └────────────────────────────────────────────────────┘
```

### 错误类型识别

- **概念误解型**: 错误应用了定义或定理
- **计算疏忽型**: 推导过程中的符号或算术错误
- **迁移困难型**: 未能将当前问题与已学例题的方法关联
- **逻辑断层型**: 步骤间缺乏必要的推理衔接

## 项目结构

```
.
├── backend/              # Spring Boot 后端
│   ├── src/main/java/    # Java源代码
│   ├── src/main/resources/
│   ├── data/             # H2数据库文件（自动创建）
│   └── pom.xml
├── frontend/             # Vue.js 前端
│   ├── src/              # 源代码
│   ├── package.json
│   └── vite.config.js
├── uploads/              # 文件上传目录（自动创建）
└── README.md
```

## API 文档

### 笔记模块

- `POST /api/notes/upload` - 上传文件
- `POST /api/notes/generate` - 生成笔记
- `GET /api/notes/list` - 获取笔记列表

### 对话模块

- `POST /api/tutor/ask` - 提问
- `GET /api/tutor/history` - 获取历史

### 诊断模块

- `POST /api/diagnose/upload` - 上传图片OCR
- `POST /api/diagnose/analyze` - 分析错误
- `GET /api/diagnose/records` - 获取诊断记录
- `GET /api/diagnose/profile` - 获取错误档案

### 练习模块

- `POST /api/exam/generate` - 生成个性化练习
- `GET /api/exam/profile/{userId}` - 获取学习档案

## 开发调试

### H2数据库控制台

访问 http://localhost:8080/h2-console 可以查看数据库内容

连接参数：
- JDBC URL: `jdbc:h2:file:./data/learning_assistant`
- 用户名: `sa`
- 密码: （留空）

### 常见问题

1. **后端启动失败**：检查JDK版本是否为17+
2. **前端启动失败**：检查Node.js版本是否为18+
3. **AI功能无法使用**：检查百度API密钥是否正确配置
4. **端口被占用**：
   - 后端默认8080端口，可在application.yml修改
   - 前端默认3000端口，可在vite.config.js修改

## 许可证

MIT License

# AI数学思维学习助手

基于"识别个人学习瓶颈-动态介入-闭环反馈"理念构建的智能学习系统。

## 系统架构

### 核心模块

1. **笔记整理官** - PPT/PDF解析，生成结构化笔记
2. **对话导师** - 苏格拉底式问答，深入理解概念
3. **解题诊断师** - 错误归因与针对性反馈
4. **出题教练** - 个性化练习生成
5. **待办管理** - 学习任务管理与追踪
6. **学习分析** - 数据看板、活跃度日历、智能学习报告

### 技术栈

- **前端**: Vue.js 3 + Element Plus + Vite
- **后端**: Java Spring Boot 3.2 + H2内存数据库（无需安装MySQL）
- **AI服务**: ChatECNU API (华东师大AI平台)

## 环境要求

- **Java**: JDK 17 或更高版本
- **Node.js**: 18 或更高版本
- **Maven**: 3.6 或更高版本（后端构建）

## 快速开始（本地运行）

### 1. 配置 AI API 密钥

#### 方式一：使用环境变量（推荐）

```bash
# Windows PowerShell
$env:AI_ECNU_API_KEY="your-api-key-here"

# Windows CMD
set AI_ECNU_API_KEY=your-api-key-here

# Linux/Mac
export AI_ECNU_API_KEY=your-api-key-here
```

#### 方式二：使用本地配置文件

复制模板文件并填入你的 API 密钥：

```bash
cp backend/src/main/resources/application-local.yml.template backend/src/main/resources/application-local.yml
```

然后编辑 `application-local.yml`：

```yaml
ai:
  ecnu:
    api-key: your-actual-api-key-here
    base-url: https://chat.ecnu.edu.cn/open/api/v1
    model: ChatECNU
```

> 获取方式：访问 [华东师大AI平台](https://chat.ecnu.edu.cn) 获取API密钥

### 2. 启动后端服务

```bash
# 进入后端目录
cd backend

# 编译并运行（第一次需要下载依赖，会比较慢）
./mvnw spring-boot:run

# Windows使用
mvnw.cmd spring-boot:run
```

后端服务将启动在 http://localhost:8081

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

前端服务将启动在 http://localhost:5173

### 4. 访问应用

打开浏览器访问 http://localhost:5173 即可使用！

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

### 学习分析功能

- **数据看板**: 总诊断次数、本周活跃度、薄弱知识点、任务完成率、笔记数量、问答次数
- **学习活跃度日历**: GitHub风格的贡献热力图，展示学习活动分布
- **智能学习报告**: AI生成的薄弱知识点Top5分析和个性化学习建议

## 项目结构

```
.
├── backend/              # Spring Boot 后端
│   ├── src/main/java/    # Java源代码
│   │   └── com/example/app/
│   │       ├── ai/           # AI服务
│   │       ├── controller/   # REST API控制器
│   │       ├── entity/       # 数据实体
│   │       ├── repository/   # 数据访问层
│   │       └── service/      # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置文件
│   │   └── application-local.yml.template  # 本地配置模板
│   └── pom.xml
├── frontend/             # Vue.js 前端
│   ├── src/
│   │   ├── api/          # API接口
│   │   ├── router/       # 路由配置
│   │   ├── views/        # 页面组件
│   │   │   ├── Home.vue      # 首页
│   │   │   ├── Notes.vue     # 笔记整理
│   │   │   ├── Tutor.vue     # 对话导师
│   │   │   ├── Diagnose.vue  # 解题诊断
│   │   │   ├── Exam.vue      # 出题练习
│   │   │   ├── Todo.vue      # 待办管理
│   │   │   └── Analysis.vue  # 学习分析
│   │   ├── App.vue
│   │   └── main.js
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

### 待办模块

- `GET /api/todos` - 获取待办列表
- `POST /api/todos` - 创建待办
- `PUT /api/todos/{id}` - 更新待办
- `DELETE /api/todos/{id}` - 删除待办

### 分析模块

- `GET /api/analysis/dashboard` - 获取数据看板
- `GET /api/analysis/calendar` - 获取活跃度日历
- `GET /api/analysis/report` - 获取智能学习报告

## Docker 部署

### 使用 Docker Compose 一键部署

```bash
# 构建并启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

访问 http://localhost 即可使用。

## 开发调试

### H2数据库控制台

访问 http://localhost:8081/h2-console 可以查看数据库内容

连接参数：
- JDBC URL: `jdbc:h2:mem:learning_assistant`
- 用户名: `sa`
- 密码: （留空）

### 常见问题

1. **后端启动失败**：检查JDK版本是否为17+
2. **前端启动失败**：检查Node.js版本是否为18+
3. **AI功能无法使用**：检查 `AI_ECNU_API_KEY` 环境变量或 `application-local.yml` 配置
4. **端口被占用**：
   - 后端默认8081端口，可在 `application.yml` 修改
   - 前端默认5173端口，可在 `vite.config.js` 修改

## 配置文件说明

### application.yml

主配置文件，包含：
- 服务器端口配置
- 数据库连接配置（H2内存数据库）
- AI服务配置（使用环境变量）
- 文件上传配置

### application-local.yml

本地开发配置文件（已加入 `.gitignore`，不会提交到Git），用于存放敏感信息如API密钥。

创建方式：
```bash
cp backend/src/main/resources/application-local.yml.template backend/src/main/resources/application-local.yml
```

## 许可证

MIT License

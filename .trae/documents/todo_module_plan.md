# 待办事项模块开发及页面布局优化计划

## 项目调整说明

根据最新需求，本次开发将：

1. **取消**"每日拓展板块"功能模块（因联网搜索实现难度过高）
2. **完成**待办事项模块的全部功能开发
3. **新增**AI问答模块的图片上传功能
4. **优化**整体页面布局结构

***

## 第一部分：待办事项模块完整开发

### 技术栈确认

**前端**

* 框架: Vue 3 + Vite

* UI组件库: Element Plus

* 路由: Vue Router 4

* HTTP客户端: Axios

* 图标: @element-plus/icons-vue

**后端**

* 框架: Spring Boot 3.2.0

* 数据库: H2（文件存储）

* ORM: Spring Data JPA

* JDK: Java 17

### 数据库设计

```sql
-- 待办事项表
CREATE TABLE todo_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    title VARCHAR(100) NOT NULL COMMENT '待办标题',
    description TEXT COMMENT '待办描述',
    due_date DATE COMMENT '截止日期',
    category VARCHAR(50) COMMENT '分类（学习/生活/其他）',
    priority INT DEFAULT 1 COMMENT '优先级（1-低 2-中 3-高）',
    is_completed BOOLEAN DEFAULT FALSE COMMENT '是否完成',
    completed_at TIMESTAMP COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### API接口设计

| 接口     | 方法     | 路径                    | 描述             |
| ------ | ------ | --------------------- | -------------- |
| 获取待办列表 | GET    | /api/todo/list        | 获取用户所有待办（支持筛选） |
| 获取待办统计 | GET    | /api/todo/stats       | 获取完成统计（用于首页卡片） |
| 创建待办   | POST   | /api/todo             | 创建新待办          |
| 更新待办   | PUT    | /api/todo/{id}        | 更新待办信息         |
| 删除待办   | DELETE | /api/todo/{id}        | 删除待办           |
| 切换完成状态 | PATCH  | /api/todo/{id}/toggle | 切换完成状态         |

### 功能需求

#### 首页卡片展示

* **位置**: 首页功能卡片区域

* **显示内容**:

  * 模块标题"待办事项"

  * 未完成待办列表（最多显示3条，超出显示"..."）

  * 每条待办右侧圆形复选框

  * 底部进度显示 "已完成数/总待办数"（如"2/3"）

* **交互**:

  * 点击复选框切换完成状态，实时更新进度

  * 点击卡片区域（除复选框外）进入详情页

#### 详情页功能

* **待办列表展示**:

  * 未完成待办列表（按截止日期排序）

  * 已完成待办列表（可折叠）

  * 每项显示：标题、描述、截止日期、完成状态

* **新建待办**:

  * 表单字段：标题（必填）、描述（选填）、截止日期（选填）、分类（选填）

  * 提交后添加到列表

* **编辑待办**:

  * 点击待办项进入编辑模式

  * 可修改所有字段

* **删除待办**:

  * 每项提供删除按钮（hover显示）

* **筛选与排序**:

  * 按分类筛选

  * 按截止日期排序

  * 按创建时间排序

  * 按优先级排序

***

## 第二部分：AI问答图片上传功能

### 功能需求

#### 用户端功能

* **图片上传入口**: 在AI问答输入区域添加图片上传按钮

* **支持格式**: JPG、PNG、GIF，单张最大5MB

* **图片预览**: 上传后显示缩略图，支持删除

* **多图支持**: 每次最多上传3张图片

* **图文混合**: 用户可同时输入文字和上传图片

#### AI处理流程
- **图片存储**: 上传图片保存到D盘指定目录 `D:/uploads/tutor/images/`，避免占用C盘空间
- **图片识别**: 调用AI服务时，将图片信息传递给AI
- **上下文关联**: 图片与对话记录关联保存

### 数据库调整

```sql
-- 在qa_history表中添加图片字段
ALTER TABLE qa_history ADD COLUMN image_urls TEXT COMMENT '图片URL列表，JSON格式存储';
```

### API接口调整

| 接口      | 方法   | 路径                      | 描述       |
| ------- | ---- | ----------------------- | -------- |
| 上传图片    | POST | /api/tutor/upload-image | 上传问答图片   |
| 提问（含图片） | POST | /api/tutor/ask          | 扩展支持图片参数 |

### 前端实现

#### 修改Tutor.vue

* 在输入区域添加图片上传按钮

* 实现图片选择、预览、删除功能

* 发送消息时携带图片信息

* 显示历史消息中的图片

***

## 第三部分：页面布局优化

### 3.1 减小左右留白

**当前问题**: 页面两侧留白过多，内容区域利用率低

**优化方案**:

* 主内容区域最大宽度从1400px调整为1200px或100%

* 减小padding值：从`padding: 24px 32px`调整为`padding: 20px 16px`

* 响应式适配：大屏设备充分利用空间，小屏保持合适边距

### 3.2 增大字体大小

**当前问题**: 字体偏小，可读性不足

**优化方案**:

| 元素      | 当前大小 | 调整后  |
| ------- | ---- | ---- |
| 页面标题    | 26px | 32px |
| 卡片标题    | 22px | 26px |
| 正文内容    | 15px | 16px |
| 小字/辅助   | 14px | 15px |
| 导航菜单    | 15px | 16px |
| Hero标题  | 42px | 48px |
| Hero副标题 | 18px | 20px |

### 3.3 模块显示优化

**需求**: 除主页外，所有功能页面仅显示当前使用的模块

**实现方案**:

* 修改App.vue布局结构

* 添加左侧导航栏组件

* 功能页面采用"左侧导航 + 右侧内容"布局

* 主页保持现有布局不变

### 3.4 左侧导航栏设计

**设计规范**:

* 宽度: 220px（固定）

* 背景色: #fff 或 #faf9f7

* 边框: 右侧1px边框 #f0eeeb

* 菜单项高度: 50px

* 图标大小: 20px

* 字体大小: 16px

**导航项**:

* 首页

* 智能笔记

* AI问答

* 错题诊断

* 智能练习

* 待办事项

**交互效果**:

* 悬停：背景色变化

* 选中：左侧边框高亮 + 文字高亮

* 过渡动画：0.3s ease

**响应式适配**:

* 大屏（>1200px）: 显示左侧导航

* 中屏（768px-1200px）: 可折叠导航

* 小屏（<768px）: 汉堡菜单

***

## 开发任务清单

### 阶段一：待办事项后端开发

| 序号 | 任务             | 文件                                 | 预计时间 |
| -- | -------------- | ---------------------------------- | ---- |
| 1  | 创建TodoItem实体类  | entity/TodoItem.java               | 15分钟 |
| 2  | 创建Repository接口 | repository/TodoItemRepository.java | 10分钟 |
| 3  | 创建Service层     | service/TodoService.java           | 40分钟 |
| 4  | 创建Controller层  | controller/TodoController.java     | 30分钟 |

### 阶段二：待办事项前端开发

| 序号 | 任务       | 文件                      | 预计时间 |
| -- | -------- | ----------------------- | ---- |
| 5  | API封装    | api/index.js            | 15分钟 |
| 6  | 创建首页卡片组件 | components/TodoCard.vue | 40分钟 |
| 7  | 创建详情页    | views/Todo.vue          | 70分钟 |
| 8  | 集成到首页    | views/Home.vue          | 15分钟 |

### 阶段三：AI问答图片上传功能

| 序号 | 任务            | 文件                              | 预计时间 |
| -- | ------------- | ------------------------------- | ---- |
| 9  | 修改QaHistory实体 | entity/QaHistory.java           | 10分钟 |
| 10 | 添加图片上传接口      | controller/TutorController.java | 20分钟 |
| 11 | 实现图片存储逻辑      | service/TutorService.java       | 20分钟 |
| 12 | 前端图片上传功能      | views/Tutor.vue                 | 40分钟 |
| 13 | API封装更新       | api/index.js                    | 10分钟 |

### 阶段四：页面布局优化

| 序号 | 任务          | 文件                     | 预计时间 |
| -- | ----------- | ---------------------- | ---- |
| 14 | 创建左侧导航栏组件   | components/Sidebar.vue | 40分钟 |
| 15 | 修改App.vue布局 | App.vue                | 30分钟 |
| 16 | 调整全局字体大小    | App.vue                | 20分钟 |
| 17 | 优化内容区域宽度    | App.vue, Home.vue等     | 20分钟 |
| 18 | 更新路由配置      | router/index.js        | 10分钟 |

### 阶段五：测试与优化

| 序号 | 任务                | 预计时间 |
| -- | ----------------- | ---- |
| 19 | 功能测试（待办事项CRUD）    | 20分钟 |
| 20 | 功能测试（图片上传）        | 15分钟 |
| 21 | UI兼容性测试（多浏览器/多尺寸） | 25分钟 |
| 22 | Bug修复与微调          | 30分钟 |

**总计预计时间**: 约8.5小时

***

## 文件结构

### 后端新增/修改

```
backend/src/main/java/com/example/app/
├── entity/
│   ├── TodoItem.java              # 新增
│   └── QaHistory.java             # 修改（添加imageUrls字段）
├── repository/
│   └── TodoItemRepository.java    # 新增
├── service/
│   ├── TodoService.java           # 新增
│   └── TutorService.java          # 修改（添加图片处理）
└── controller/
    ├── TodoController.java        # 新增
    └── TutorController.java       # 修改（添加图片上传接口）
```

### 前端新增/修改

```
frontend/src/
├── api/
│   └── index.js                   # 添加todoApi和tutor图片上传
├── components/
│   ├── TodoCard.vue               # 待办首页卡片（新增）
│   └── Sidebar.vue                # 左侧导航栏（新增）
├── views/
│   ├── Todo.vue                   # 待办详情页（新增）
│   ├── Tutor.vue                  # 修改（添加图片上传功能）
│   ├── Home.vue                   # 修改布局
│   ├── Notes.vue                  # 修改布局
│   ├── Diagnose.vue               # 修改布局
│   └── Exam.vue                   # 修改布局
├── App.vue                        # 修改整体布局
└── router/index.js                # 添加路由
```

***

## 验收标准

### 待办事项模块

* [ ] 首页卡片显示待办列表（最多3条）

* [ ] 复选框可切换完成状态，实时更新进度

* [ ] 进度显示正确（已完成/总数）

* [ ] 点击卡片进入详情页

* [ ] 详情页支持新建、编辑、删除待办

* [ ] 支持筛选（分类）和排序（截止日期/创建时间/优先级）

* [ ] 数据持久化到H2数据库

### AI问答图片上传功能

* [ ] 输入区域显示图片上传按钮

* [ ] 支持选择JPG/PNG/GIF格式图片

* [ ] 图片上传后显示预览缩略图

* [ ] 支持删除已选图片

* [ ] 发送消息时图片正常传递给AI

* [ ] 历史对话中正确显示图片

### 页面布局优化

* [ ] 左右留白减小，内容区域更宽

* [ ] 字体大小增大，层级清晰

* [ ] 左侧导航栏正常显示

* [ ] 功能页面（除首页）使用左侧导航布局

* [ ] 导航切换流畅，交互正常

* [ ] 响应式适配不同屏幕尺寸

* [ ] 样式与现有莫兰迪色系保持一致

### 整体要求

* [ ] 所有功能正常运行

* [ ] 主流浏览器兼容（Chrome/Firefox/Edge）

* [ ] 代码符合项目规范

* [ ] 无明显Bug


<template>
  <div class="home">
    <!-- Hero区域 - 更紧凑的设计 -->
    <div class="hero">
      <div class="hero-content">
        <h1 class="hero-title">AI数学思维学习助手</h1>
        <p class="hero-subtitle">识别个人学习瓶颈 · 动态介入 · 闭环反馈</p>
      </div>
    </div>

    <!-- 功能模块卡片 - 紧凑布局 -->
    <div class="modules">
      <el-row :gutter="16">
        <el-col :span="8" v-for="module in modules" :key="module.name">
          <el-card class="module-card" shadow="hover" @click="$router.push(module.path)">
            <div class="module-content">
              <div class="module-icon-wrapper" :style="{ backgroundColor: module.bgColor }">
                <el-icon :size="28" :color="module.color">
                  <component :is="module.icon" />
                </el-icon>
              </div>
              <div class="module-info">
                <h3>{{ module.title }}</h3>
                <p>{{ module.description }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 待办事项和核心功能 - 并排布局 -->
    <el-row :gutter="16" class="bottom-section">
      <!-- 待办事项卡片 -->
      <el-col :span="10">
        <el-card class="todo-card" shadow="hover" @click="$router.push('/todo')">
          <template #header>
            <div class="todo-card-header">
              <div class="todo-title-wrapper">
                <el-icon :size="18" color="#9db4c4"><List /></el-icon>
                <span class="todo-card-title">待办事项</span>
              </div>
              <span class="todo-progress">{{ stats.completed }}/{{ stats.total }}</span>
            </div>
          </template>
          <div class="todo-list-preview">
            <div
              v-for="todo in todos.slice(0, 3)"
              :key="todo.id"
              class="todo-preview-item"
              @click.stop
            >
              <el-checkbox
                :model-value="todo.completed"
                @change="() => toggleTodo(todo)"
                class="todo-checkbox"
              />
              <span class="todo-preview-text" :class="{ completed: todo.completed }">
                {{ todo.title }}
              </span>
            </div>
            <div v-if="pendingTodos.length === 0 && stats.total > 0" class="all-completed">
              <el-icon color="#7a9e7e"><CircleCheck /></el-icon>
              <span>所有待办已完成！</span>
            </div>
            <div v-if="pendingTodos.length > 3" class="more-todos">
              还有 {{ pendingTodos.length - 3 }} 个待办...
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 核心功能 -->
      <el-col :span="14">
        <div class="features">
          <div class="features-header">
            <h2 class="section-title">核心功能</h2>
          </div>
          <el-row :gutter="12">
            <el-col :span="8" v-for="feature in features" :key="feature.title">
              <div class="feature-item">
                <div class="feature-icon-wrapper" :style="{ backgroundColor: feature.bgColor }">
                  <el-icon :size="22" :color="feature.color"><component :is="feature.icon" /></el-icon>
                </div>
                <h4>{{ feature.title }}</h4>
                <p>{{ feature.desc }}</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { todoApi } from '../api'
import { List, CircleCheck } from '@element-plus/icons-vue'

const userId = 'user001'
const todos = ref([])
const stats = ref({ total: 0, completed: 0, pending: 0 })

const pendingTodos = computed(() => {
  return todos.value.filter(t => !t.completed)
})

const loadTodos = async () => {
  try {
    const [todosRes, statsRes] = await Promise.all([
      todoApi.getList(userId, false), // 只获取未完成的待办
      todoApi.getStats(userId)
    ])
    todos.value = todosRes.data
    stats.value = statsRes.data
  } catch (error) {
    console.error('加载待办事项失败', error)
  }
}

const toggleTodo = async (todo) => {
  try {
    const response = await todoApi.toggle(todo.id, userId)
    todo.completed = response.data.completed
    ElMessage.success(todo.completed ? '已完成' : '已标记为未完成')
    const statsRes = await todoApi.getStats(userId)
    stats.value = statsRes.data
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadTodos()
})

// Morandi色系配色方案
const modules = [
  {
    name: 'notes',
    title: '智能笔记',
    description: '上传PPT/PDF，AI生成结构化学习笔记',
    icon: 'Document',
    color: '#7a9e7e',
    bgColor: 'rgba(122, 158, 126, 0.15)',
    path: '/notes'
  },
  {
    name: 'tutor',
    title: 'AI问答',
    description: '随时提问，AI用通俗语言解答疑惑',
    icon: 'ChatDotRound',
    color: '#6b8cae',
    bgColor: 'rgba(107, 140, 174, 0.15)',
    path: '/tutor'
  },
  {
    name: 'diagnose',
    title: '解题诊断',
    description: '上传题目，AI分析解答并给出建议',
    icon: 'Search',
    color: '#c4a77d',
    bgColor: 'rgba(196, 167, 125, 0.15)',
    path: '/diagnose'
  },
  {
    name: 'exam',
    title: '智能练习',
    description: '根据薄弱点，AI生成针对性练习题',
    icon: 'Edit',
    color: '#b88a8a',
    bgColor: 'rgba(184, 138, 138, 0.15)',
    path: '/exam'
  },
  {
    name: 'todo',
    title: '待办事项',
    description: '管理学习任务，追踪完成进度',
    icon: 'List',
    color: '#9db4c4',
    bgColor: 'rgba(157, 180, 196, 0.15)',
    path: '/todo'
  },
  {
    name: 'analysis',
    title: '学习分析',
    description: '查看学习数据，了解进步轨迹',
    icon: 'TrendCharts',
    color: '#a5b5a5',
    bgColor: 'rgba(165, 181, 165, 0.15)',
    path: '/analysis'
  }
]

const features = [
  {
    title: '学习闭环',
    desc: '从诊断到练习的完整学习闭环',
    icon: 'Refresh',
    color: '#8fa3b8',
    bgColor: 'rgba(143, 163, 184, 0.15)'
  },
  {
    title: '个性化推荐',
    desc: '基于错误档案的智能推荐',
    icon: 'User',
    color: '#a5b5a5',
    bgColor: 'rgba(165, 181, 165, 0.15)'
  },
  {
    title: 'AI驱动',
    desc: '大模型赋能的智能学习助手',
    icon: 'Cpu',
    color: '#b8a5a5',
    bgColor: 'rgba(184, 165, 165, 0.15)'
  }
]
</script>

<style scoped>
.home {
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero区域 - 使用图片中的渐变配色：浅蓝 -> 粉紫 -> 浅橙黄 */
.hero {
  text-align: center;
  padding: 32px 40px;
  background: linear-gradient(90deg, #a8c8e8 0%, #c8a8d8 35%, #e8c8a8 70%, #f0d890 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  color: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.hero-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 14px;
  opacity: 0.95;
  font-weight: 400;
  letter-spacing: 1px;
}

/* 模块卡片区域 - 紧凑布局 */
.modules {
  margin-bottom: 16px;
}

.module-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 10px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.module-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.module-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.module-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.module-card:hover .module-icon-wrapper {
  transform: scale(1.05);
}

.module-info h3 {
  font-size: 16px;
  margin-bottom: 4px;
  color: #3a3a3a;
  font-weight: 600;
}

.module-info p {
  color: #888;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
}

/* 底部区域 - 并排布局 */
.bottom-section {
  margin-top: 0;
}

/* 待办事项卡片 */
.todo-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 10px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 100%;
}

.todo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.todo-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f0eeeb;
}

.todo-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #3a3a3a;
}

.todo-progress {
  font-size: 14px;
  font-weight: 500;
  color: #9db4c4;
  background: rgba(157, 180, 196, 0.15);
  padding: 2px 10px;
  border-radius: 12px;
}

.todo-list-preview {
  padding: 4px 0;
}

.todo-preview-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f3f0;
}

.todo-preview-item:last-child {
  border-bottom: none;
}

.todo-preview-text {
  font-size: 13px;
  color: #3a3a3a;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-preview-text.completed {
  text-decoration: line-through;
  color: #999;
}

.all-completed {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px 0;
  color: #7a9e7e;
  font-size: 13px;
}

.more-todos {
  text-align: center;
  padding: 8px 0;
  color: #999;
  font-size: 12px;
}

/* 核心功能区域 */
.features {
  background: #faf9f7;
  padding: 16px 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  height: 100%;
}

.features-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  color: #3a3a3a;
  font-weight: 600;
  margin: 0;
}

.feature-item {
  text-align: center;
  padding: 12px 8px;
  background: #fff;
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.feature-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  transition: transform 0.3s ease;
}

.feature-item:hover .feature-icon-wrapper {
  transform: scale(1.08);
}

.feature-item h4 {
  font-size: 13px;
  margin: 0 0 6px;
  color: #3a3a3a;
  font-weight: 600;
}

.feature-item p {
  color: #888;
  font-size: 11px;
  line-height: 1.5;
  margin: 0;
}
</style>

<template>
  <div class="todo-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><List /></el-icon>
        待办事项
      </h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建待办
      </el-button>
    </div>

    <el-row :gutter="24">
      <el-col :span="16">
        <el-card class="todo-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>待办列表</span>
              <div class="header-actions">
                <!-- 排序选择 -->
                <el-select v-model="sortBy" size="small" style="width: 120px; margin-right: 12px;">
                  <el-option label="按时间排序" value="time" />
                  <el-option label="按优先级排序" value="priority" />
                  <el-option label="按截止日期排序" value="dueDate" />
                </el-select>
                <el-radio-group v-model="filterStatus" size="small">
                  <el-radio-button label="all">全部</el-radio-button>
                  <el-radio-button label="pending">未完成</el-radio-button>
                  <el-radio-button label="completed">已完成</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>

          <div v-if="sortedTodos.length === 0" class="empty-state">
            <el-empty description="暂无待办事项" :image-size="100">
              <el-button type="primary" @click="showCreateDialog = true">创建第一个待办</el-button>
            </el-empty>
          </div>

          <div v-else class="todo-list">
            <div
              v-for="todo in sortedTodos"
              :key="todo.id"
              :class="['todo-item', { completed: todo.completed, overdue: isOverdue(todo) }]"
            >
              <el-checkbox
                v-model="todo.completed"
                @change="toggleTodo(todo)"
                class="todo-checkbox"
              />
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div v-if="todo.description" class="todo-desc">{{ todo.description }}</div>
                <div class="todo-meta">
                  <el-tag v-if="todo.priority === 3" type="danger" size="small">高优先级</el-tag>
                  <el-tag v-else-if="todo.priority === 2" type="warning" size="small">中优先级</el-tag>
                  <el-tag v-else type="info" size="small">低优先级</el-tag>
                  <span class="todo-time">{{ formatDate(todo.createdAt) }}</span>
                  <span v-if="todo.dueDate" :class="['due-date', { overdue: isOverdue(todo) }]">
                    <el-icon><Clock /></el-icon>
                    {{ formatDueDate(todo.dueDate) }}
                    <span v-if="isOverdue(todo)" class="overdue-tag">已逾期</span>
                  </span>
                </div>
              </div>
              <div class="todo-actions">
                <el-button
                  type="primary"
                  text
                  size="small"
                  @click="editTodo(todo)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  type="danger"
                  text
                  size="small"
                  @click="confirmDelete(todo)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stats-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>完成进度</span>
            </div>
          </template>
          <div class="stats-content">
            <div class="progress-circle">
              <el-progress
                type="circle"
                :percentage="completionRate"
                :color="progressColor"
                :stroke-width="10"
                :width="120"
              />
            </div>
            <div class="stats-detail">
              <div class="stat-item">
                <span class="stat-label">总待办</span>
                <span class="stat-value">{{ stats.total }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">已完成</span>
                <span class="stat-value completed">{{ stats.completed }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">待完成</span>
                <span class="stat-value pending">{{ stats.pending }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="tips-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>使用技巧</span>
            </div>
          </template>
          <ul class="tips-list">
            <li>
              <el-icon><Check /></el-icon>
              <span>点击复选框快速标记完成状态</span>
            </li>
            <li>
              <el-icon><Check /></el-icon>
              <span>设置截止日期避免遗漏重要事项</span>
            </li>
            <li>
              <el-icon><Check /></el-icon>
              <span>设置优先级帮助区分重要程度</span>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingTodo ? '编辑待办' : '新建待办'"
      width="500px"
    >
      <el-form :model="todoForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="todoForm.title" placeholder="请输入待办事项标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="todoForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入详细描述（可选）"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="todoForm.priority">
            <el-radio-button :label="1">低</el-radio-button>
            <el-radio-button :label="2">中</el-radio-button>
            <el-radio-button :label="3">高</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="todoForm.dueDate"
            type="datetime"
            placeholder="选择截止日期（可选）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveTodo" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { todoApi } from '../api'
import { List, Plus, Edit, Delete, TrendCharts, InfoFilled, Check, Clock } from '@element-plus/icons-vue'

const userId = 'user001'
const todos = ref([])
const stats = ref({ total: 0, completed: 0, pending: 0 })
const filterStatus = ref('all')
const sortBy = ref('time')
const showCreateDialog = ref(false)
const saving = ref(false)
const editingTodo = ref(null)

const todoForm = ref({
  title: '',
  description: '',
  priority: 1,
  dueDate: null
})

const filteredTodos = computed(() => {
  if (filterStatus.value === 'pending') {
    return todos.value.filter(t => !t.completed)
  } else if (filterStatus.value === 'completed') {
    return todos.value.filter(t => t.completed)
  }
  return todos.value
})

const sortedTodos = computed(() => {
  const filtered = filteredTodos.value
  return [...filtered].sort((a, b) => {
    if (sortBy.value === 'priority') {
      // 优先级高的在前
      return (b.priority || 1) - (a.priority || 1)
    } else if (sortBy.value === 'dueDate') {
      // 有截止日期的在前，按截止日期排序
      if (!a.dueDate && !b.dueDate) return 0
      if (!a.dueDate) return 1
      if (!b.dueDate) return -1
      return new Date(a.dueDate) - new Date(b.dueDate)
    } else {
      // 按时间排序（最新的在前）
      return new Date(b.createdAt) - new Date(a.createdAt)
    }
  })
})

const completionRate = computed(() => {
  if (stats.value.total === 0) return 0
  return Math.round((stats.value.completed / stats.value.total) * 100)
})

const progressColor = computed(() => {
  if (completionRate.value >= 80) return '#7a9e7e'
  if (completionRate.value >= 50) return '#c4a77d'
  return '#b88a8a'
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDueDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const due = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  const diffDays = Math.floor((due - today) / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    return '明天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === -1) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

const isOverdue = (todo) => {
  if (!todo.dueDate || todo.completed) return false
  return new Date(todo.dueDate) < new Date()
}

const loadTodos = async () => {
  try {
    const response = await todoApi.getList(userId)
    todos.value = response.data
  } catch (error) {
    ElMessage.error('加载待办事项失败')
  }
}

const loadStats = async () => {
  try {
    const response = await todoApi.getStats(userId)
    stats.value = response.data
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

const toggleTodo = async (todo) => {
  try {
    const response = await todoApi.toggle(todo.id, userId)
    // 更新本地数据，确保UI立即响应
    todo.completed = response.data.completed
    ElMessage.success(todo.completed ? '已完成' : '已标记为未完成')
    // 重新加载统计数据
    loadStats()
  } catch (error) {
    ElMessage.error('操作失败')
    todo.completed = !todo.completed
  }
}

const editTodo = (todo) => {
  editingTodo.value = todo
  todoForm.value = {
    title: todo.title,
    description: todo.description || '',
    priority: todo.priority || 1,
    dueDate: todo.dueDate || null
  }
  showCreateDialog.value = true
}

const closeDialog = () => {
  showCreateDialog.value = false
  editingTodo.value = null
  todoForm.value = { title: '', description: '', priority: 1, dueDate: null }
}

const saveTodo = async () => {
  if (!todoForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  saving.value = true
  try {
    if (editingTodo.value) {
      await todoApi.update(editingTodo.value.id, {
        userId,
        title: todoForm.value.title,
        description: todoForm.value.description,
        priority: todoForm.value.priority,
        dueDate: todoForm.value.dueDate
      })
      ElMessage.success('更新成功')
    } else {
      await todoApi.create({
        userId,
        title: todoForm.value.title,
        description: todoForm.value.description,
        priority: todoForm.value.priority,
        dueDate: todoForm.value.dueDate
      })
      ElMessage.success('创建成功')
    }
    closeDialog()
    loadTodos()
    loadStats()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const confirmDelete = async (todo) => {
  try {
    await ElMessageBox.confirm('确定要删除这个待办事项吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await todoApi.delete(todo.id, userId)
    ElMessage.success('删除成功')
    loadTodos()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadTodos()
  loadStats()
})
</script>

<style scoped>
.todo-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 26px;
  color: #3a3a3a;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.todo-card {
  min-height: 500px;
}

.empty-state {
  padding: 60px 0;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #faf9f7;
  border-radius: 8px;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.todo-item:hover {
  background: #f0eeeb;
}

.todo-item.completed {
  opacity: 0.7;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: #999;
}

.todo-item.overdue {
  border-left-color: #f56c6c;
  background: #fef0f0;
}

.todo-checkbox {
  margin-top: 2px;
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 15px;
  font-weight: 500;
  color: #3a3a3a;
  margin-bottom: 4px;
}

.todo-desc {
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
  line-height: 1.5;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.todo-time {
  font-size: 12px;
  color: #aaa;
}

.due-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.due-date.overdue {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.overdue-tag {
  font-size: 10px;
  color: #fff;
  background: #f56c6c;
  padding: 0 4px;
  border-radius: 2px;
  margin-left: 4px;
}

.todo-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.todo-item:hover .todo-actions {
  opacity: 1;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
}

.progress-circle {
  display: flex;
  justify-content: center;
}

.stats-detail {
  display: flex;
  justify-content: space-around;
  width: 100%;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #888;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #3a3a3a;
}

.stat-value.completed {
  color: #7a9e7e;
}

.stat-value.pending {
  color: #b88a8a;
}

.tips-card :deep(.el-card__header) {
  background: #faf9f7;
  border-bottom: 1px solid #f0eeeb;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #3a3a3a;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 0;
  color: #555;
  font-size: 14px;
  line-height: 1.6;
  border-bottom: 1px solid #f5f3f0;
}

.tips-list li:last-child {
  border-bottom: none;
}

.tips-list li :deep(.el-icon) {
  color: #7a9e7e;
  margin-top: 2px;
  flex-shrink: 0;
}
</style>

<template>
  <div class="todo-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><CircleCheck /></el-icon>
        待办事项
      </h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新建待办
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总待办</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-value" style="color: #7a9e7e;">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-value" style="color: #c4a77d;">{{ stats.pending }}</div>
          <div class="stat-label">待完成</div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 120px;">
        <el-option label="全部" value="all" />
        <el-option label="未完成" value="pending" />
        <el-option label="已完成" value="completed" />
      </el-select>
      <el-select v-model="sortBy" placeholder="排序方式" style="width: 140px;">
        <el-option label="按截止日期" value="dueDate" />
        <el-option label="按创建时间" value="createdAt" />
        <el-option label="按优先级" value="priority" />
      </el-select>
    </div>

    <!-- 待办列表 -->
    <el-card class="todo-list-card" shadow="never">
      <template #header>
        <div class="list-header">
          <span>未完成</span>
          <el-tag type="warning">{{ pendingTodos.length }}</el-tag>
        </div>
      </template>

      <div v-if="pendingTodos.length === 0" class="empty-state">
        <el-empty description="暂无待办事项" :image-size="100" />
      </div>

      <div v-else class="todo-items">
        <div
          v-for="todo in pendingTodos"
          :key="todo.id"
          class="todo-item"
          @click="editTodo(todo)"
        >
          <div class="todo-main">
            <div
              class="checkbox"
              @click.stop="toggleTodo(todo.id)"
            >
              <div class="checkbox-inner"></div>
            </div>
            <div class="todo-content">
              <div class="todo-title">{{ todo.title }}</div>
              <div v-if="todo.description" class="todo-desc">{{ todo.description }}</div>
              <div class="todo-meta">
                <el-tag v-if="todo.dueDate" size="small" :type="getDueTagType(todo.dueDate)">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(todo.dueDate) }}
                </el-tag>
                <el-tag v-if="todo.category" size="small" type="info">{{ todo.category }}</el-tag>
                <el-tag v-if="todo.priority" size="small" :type="getPriorityType(todo.priority)">
                  {{ getPriorityLabel(todo.priority) }}
                </el-tag>
              </div>
            </div>
          </div>
          <div class="todo-actions">
            <el-button
              type="danger"
              text
              size="small"
              @click.stop="deleteTodo(todo.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 已完成列表 -->
    <el-card class="todo-list-card completed-card" shadow="never">
      <template #header>
        <div class="list-header" @click="showCompleted = !showCompleted">
          <div class="header-left">
            <el-icon class="toggle-icon" :class="{ rotated: showCompleted }">
              <ArrowDown />
            </el-icon>
            <span>已完成</span>
            <el-tag type="success">{{ completedTodos.length }}</el-tag>
          </div>
        </div>
      </template>

      <div v-show="showCompleted" class="todo-items completed">
        <div
          v-for="todo in completedTodos"
          :key="todo.id"
          class="todo-item completed"
        >
          <div class="todo-main">
            <div
              class="checkbox checked"
              @click.stop="toggleTodo(todo.id)"
            >
              <el-icon :size="14" color="#fff"><Check /></el-icon>
            </div>
            <div class="todo-content">
              <div class="todo-title completed">{{ todo.title }}</div>
              <div v-if="todo.completedAt" class="todo-meta">
                <span class="completed-time">
                  完成于 {{ formatDateTime(todo.completedAt) }}
                </span>
              </div>
            </div>
          </div>
          <div class="todo-actions">
            <el-button
              type="danger"
              text
              size="small"
              @click.stop="deleteTodo(todo.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEditing ? '编辑待办' : '新建待办'"
      width="600px"
      class="todo-dialog"
    >
      <el-form :model="todoForm" label-width="100px" class="todo-form">
        <el-form-item label="标题" required>
          <el-input 
            v-model="todoForm.title" 
            placeholder="请输入待办标题"
            size="large"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="todoForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入描述（可选）"
            size="large"
          />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="todoForm.dueDate"
            type="date"
            placeholder="选择截止日期"
            style="width: 100%;"
            size="large"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select 
            v-model="todoForm.category" 
            placeholder="选择分类" 
            clearable 
            style="width: 100%;"
            size="large"
          >
            <el-option label="学习" value="学习" />
            <el-option label="生活" value="生活" />
            <el-option label="工作" value="工作" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="todoForm.priority" size="large">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showAddDialog = false">取消</el-button>
        <el-button size="large" type="primary" @click="saveTodo">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleCheck,
  Plus,
  Delete,
  Calendar,
  Check,
  ArrowDown
} from '@element-plus/icons-vue'
import { todoApi } from '../api'

const userId = 'user001'
const todos = ref([])
const stats = ref({ total: 0, completed: 0, pending: 0 })
const showAddDialog = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const showCompleted = ref(false)
const filterStatus = ref('all')
const sortBy = ref('dueDate')

const todoForm = ref({
  title: '',
  description: '',
  dueDate: null,
  category: '',
  priority: 1,
  userId: userId
})

const pendingTodos = computed(() => {
  let result = todos.value.filter(t => !t.isCompleted)
  return sortTodos(result)
})

const completedTodos = computed(() => {
  let result = todos.value.filter(t => t.isCompleted)
  return sortTodos(result)
})

const sortTodos = (list) => {
  if (sortBy.value === 'dueDate') {
    return list.sort((a, b) => {
      if (!a.dueDate && !b.dueDate) return 0
      if (!a.dueDate) return 1
      if (!b.dueDate) return -1
      return new Date(a.dueDate) - new Date(b.dueDate)
    })
  } else if (sortBy.value === 'priority') {
    return list.sort((a, b) => (b.priority || 1) - (a.priority || 1))
  }
  return list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
}

const loadTodos = async () => {
  try {
    const [todosRes, statsRes] = await Promise.all([
      todoApi.getList(userId),
      todoApi.getStats(userId)
    ])
    todos.value = todosRes.data || []
    stats.value = statsRes.data || { total: 0, completed: 0, pending: 0 }
  } catch (error) {
    ElMessage.error('加载待办事项失败')
  }
}

const toggleTodo = async (id) => {
  try {
    await todoApi.toggle(id)
    await loadTodos()
    ElMessage.success('状态已更新')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteTodo = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条待办事项吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await todoApi.delete(id)
    await loadTodos()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const editTodo = (todo) => {
  isEditing.value = true
  editingId.value = todo.id
  todoForm.value = {
    title: todo.title,
    description: todo.description || '',
    dueDate: todo.dueDate,
    category: todo.category || '',
    priority: todo.priority || 1,
    userId: userId
  }
  showAddDialog.value = true
}

const saveTodo = async () => {
  if (!todoForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  try {
    const data = {
      ...todoForm.value,
      dueDate: todoForm.value.dueDate ? formatDateForApi(todoForm.value.dueDate) : null
    }

    if (isEditing.value) {
      await todoApi.update(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await todoApi.create(data)
      ElMessage.success('创建成功')
    }

    showAddDialog.value = false
    resetForm()
    await loadTodos()
  } catch (error) {
    ElMessage.error(isEditing.value ? '更新失败' : '创建失败')
  }
}

const resetForm = () => {
  isEditing.value = false
  editingId.value = null
  todoForm.value = {
    title: '',
    description: '',
    dueDate: null,
    category: '',
    priority: 1,
    userId: userId
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const formatDateForApi = (date) => {
  if (!date) return null
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const getDueTagType = (dueDate) => {
  if (!dueDate) return ''
  const today = new Date()
  const due = new Date(dueDate)
  const diff = Math.ceil((due - today) / (1000 * 60 * 60 * 24))
  if (diff < 0) return 'danger'
  if (diff <= 3) return 'warning'
  return 'info'
}

const getPriorityType = (priority) => {
  if (priority === 3) return 'danger'
  if (priority === 2) return 'warning'
  return 'info'
}

const getPriorityLabel = (priority) => {
  if (priority === 3) return '高'
  if (priority === 2) return '中'
  return '低'
}

watch(showAddDialog, (val) => {
  if (!val) resetForm()
})

onMounted(() => {
  loadTodos()
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
  margin-bottom: 28px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
}

.page-title .el-icon {
  color: #f6d365;
}

.stats-row {
  margin-bottom: 28px;
}

.stat-card {
  background: linear-gradient(135deg, #fff 0%, #fffaf0 100%);
  border-radius: 16px;
  padding: 28px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid #fef3c7;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(246, 211, 101, 0.2);
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 15px;
  color: #718096;
  font-weight: 500;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.todo-list-card {
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
}

.completed-card :deep(.el-card__header) {
  cursor: pointer;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  color: #1a202c;
  font-size: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-icon {
  transition: transform 0.3s ease;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

.empty-state {
  padding: 40px 0;
}

.todo-items {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 18px;
  background: linear-gradient(135deg, #fff 0%, #fffaf0 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #fef3c7;
}

.todo-item:hover {
  background: #fff;
  border-color: #f6d365;
  box-shadow: 0 4px 16px rgba(246, 211, 101, 0.15);
}

.todo-item.completed {
  opacity: 0.7;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-color: #e2e8f0;
}

.todo-main {
  display: flex;
  gap: 14px;
  flex: 1;
}

.checkbox {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #f6d365;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  margin-top: 2px;
  transition: all 0.2s ease;
}

.checkbox:hover {
  background: rgba(246, 211, 101, 0.15);
}

.checkbox.checked {
  background: #48bb78;
  border-color: #48bb78;
}

.checkbox-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #f6d365;
  opacity: 0;
  transition: opacity 0.2s;
}

.checkbox:hover .checkbox-inner {
  opacity: 0.5;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
}

.todo-title.completed {
  text-decoration: line-through;
  color: #a0aec0;
}

.todo-desc {
  font-size: 14px;
  color: #718096;
  margin-bottom: 10px;
  line-height: 1.5;
}

.todo-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.todo-meta :deep(.el-tag) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.completed-time {
  font-size: 13px;
  color: #a0aec0;
}

.todo-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.todo-item:hover .todo-actions {
  opacity: 1;
}

/* 弹窗样式 */
.todo-dialog :deep(.el-dialog__header) {
  padding: 24px 30px;
}

.todo-dialog :deep(.el-dialog__title) {
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
}

.todo-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.todo-dialog :deep(.el-dialog__footer) {
  padding: 20px 30px;
}

.todo-form :deep(.el-form-item__label) {
  font-size: 15px;
  font-weight: 600;
  height: 44px;
  line-height: 44px;
  color: #4a5568;
}

.todo-form :deep(.el-form-item__content) {
  font-size: 15px;
}

.todo-form :deep(.el-input__inner) {
  font-size: 15px;
  height: 44px;
}

.todo-form :deep(.el-textarea__inner) {
  font-size: 15px;
}

.todo-form :deep(.el-radio__label) {
  font-size: 15px;
}

.todo-form :deep(.el-button) {
  font-size: 15px;
  padding: 12px 28px;
}
</style>

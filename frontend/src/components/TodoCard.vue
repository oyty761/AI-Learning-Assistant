<template>
  <div class="todo-card" @click="goToTodoPage">
    <div class="card-header">
      <div class="icon-wrapper" :style="{ backgroundColor: 'rgba(196, 167, 125, 0.15)' }">
        <el-icon :size="32" color="#c4a77d">
          <CircleCheck />
        </el-icon>
      </div>
      <div class="title-section">
        <h3 class="card-title">待办事项</h3>
        <span class="progress-text">{{ completedCount }}/{{ totalCount }}</span>
      </div>
    </div>

    <div class="todo-list">
      <div
        v-for="todo in displayTodos"
        :key="todo.id"
        class="todo-item"
        @click.stop="toggleTodo(todo.id)"
      >
        <span class="todo-title" :class="{ completed: todo.isCompleted }">
          {{ truncate(todo.title, 14) }}
        </span>
        <div
          class="checkbox"
          :class="{ checked: todo.isCompleted }"
          @click.stop="toggleTodo(todo.id)"
        >
          <el-icon v-if="todo.isCompleted" :size="14" color="#fff">
            <Check />
          </el-icon>
        </div>
      </div>
      <div v-if="hasMore" class="more-indicator">...</div>
    </div>

    <div class="card-footer">
      <span class="hint">点击管理待办</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, Check } from '@element-plus/icons-vue'
import { todoApi } from '../api'

const router = useRouter()
const todos = ref([])
const stats = ref({ total: 0, completed: 0, pending: 0 })
const userId = 'user001'

const completedCount = computed(() => stats.value.completed || 0)
const totalCount = computed(() => stats.value.total || 0)

const displayTodos = computed(() => {
  return todos.value.slice(0, 3)
})

const hasMore = computed(() => {
  return todos.value.length > 3
})

const truncate = (str, length) => {
  if (!str) return ''
  return str.length > length ? str.substring(0, length) + '...' : str
}

const loadTodos = async () => {
  try {
    const [todosRes, statsRes] = await Promise.all([
      todoApi.getByStatus(userId, false),
      todoApi.getStats(userId)
    ])
    todos.value = todosRes.data || []
    stats.value = statsRes.data || { total: 0, completed: 0, pending: 0 }
  } catch (error) {
    console.error('加载待办事项失败', error)
  }
}

const toggleTodo = async (id) => {
  try {
    await todoApi.toggle(id)
    await loadTodos()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToTodoPage = () => {
  router.push('/todo')
}

onMounted(() => {
  loadTodos()
})
</script>

<style scoped>
.todo-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  height: 100%;
  border: 1px solid #e2e8f0;
}

.todo-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  border-color: #cbd5e0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(237, 137, 54, 0.12);
}

.icon-wrapper .el-icon {
  color: #ed8936;
}

.title-section {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
}

.progress-text {
  font-size: 20px;
  font-weight: 700;
  color: #ed8936;
}

.todo-list {
  margin-bottom: 20px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item:hover {
  background: #f7fafc;
  margin: 0 -14px;
  padding-left: 14px;
  padding-right: 14px;
  border-radius: 8px;
}

.todo-title {
  font-size: 15px;
  color: #2d3748;
  flex: 1;
  font-weight: 500;
}

.todo-title.completed {
  text-decoration: line-through;
  color: #a0aec0;
}

.checkbox {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #ed8936;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.checkbox:hover {
  background: rgba(237, 137, 54, 0.1);
}

.checkbox.checked {
  background: #ed8936;
  border-color: #ed8936;
}

.more-indicator {
  text-align: center;
  color: #a0aec0;
  font-size: 16px;
  padding: 10px 0;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.hint {
  font-size: 14px;
  color: #a0aec0;
  font-weight: 500;
}
</style>

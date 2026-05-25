<template>
  <div class="tutor-page">
    <h2 class="page-title">
      <el-icon><ChatDotRound /></el-icon>
      对话导师
    </h2>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chat-card">
          <div class="chat-container" ref="chatContainer">
            <div v-for="(msg, index) in messages" :key="index" 
                 :class="['message', msg.role]">
              <div class="message-avatar">
                <el-avatar :size="40" :icon="msg.role === 'user' ? UserFilled : School" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="role-name">{{ msg.role === 'user' ? '我' : 'AI导师' }}</span>
                  <span class="time">{{ formatTime(msg.time) }}</span>
                </div>
                <div class="message-body" v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>
            
            <div v-if="loading" class="message ai">
              <div class="message-avatar">
                <el-avatar :size="40" :icon="School" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="role-name">AI导师</span>
                </div>
                <div class="message-body">
                  <el-skeleton :rows="2" animated />
                </div>
              </div>
            </div>
          </div>

          <div class="input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入你的问题..."
              @keyup.enter.ctrl="sendMessage"
            />
            <div class="input-actions">
              <span class="hint">Ctrl + Enter 发送</span>
              <el-button 
                type="primary" 
                @click="sendMessage"
                :loading="loading"
                :disabled="!inputMessage.trim()"
              >
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>问答历史</span>
            </div>
          </template>
          
          <div v-if="history.length === 0" class="empty-history">
            <el-empty description="暂无问答记录" />
          </div>
          
          <div v-else class="history-list">
            <div 
              v-for="item in history" 
              :key="item.id"
              class="history-item"
              @click="loadHistoryItem(item)"
            >
              <div class="history-question">{{ truncate(item.question, 30) }}</div>
              <div class="history-time">{{ formatDate(item.createdAt) }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="tips-card">
          <template #header>
            <div class="card-header">
              <span>提问技巧</span>
            </div>
          </template>
          
          <ul class="tips-list">
            <li>尽量描述具体的问题场景</li>
            <li>说明你已经尝试过的方法</li>
            <li>指出你不理解的具体步骤</li>
            <li>可以要求AI举具体例子</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { tutorApi } from '../api'
import { UserFilled, School, Promotion } from '@element-plus/icons-vue'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const history = ref([])
const chatContainer = ref(null)

const userId = 'user001'

const formatTime = (time) => {
  return new Date(time).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const truncate = (str, length) => {
  if (!str) return ''
  return str.length > length ? str.substring(0, length) + '...' : str
}

const renderMarkdown = (content) => {
  return marked(content)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const question = inputMessage.value.trim()
  if (!question || loading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: question,
    time: new Date()
  })
  
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const response = await tutorApi.ask(userId, question)
    
    messages.value.push({
      role: 'assistant',
      content: response.data.answer,
      time: new Date()
    })
    
    loadHistory()
  } catch (error) {
    ElMessage.error('发送失败: ' + error.message)
    messages.value.pop()
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const loadHistory = async () => {
  try {
    const response = await tutorApi.getHistory(userId)
    history.value = response.data
  } catch (error) {
    console.error('加载历史失败', error)
  }
}

const loadHistoryItem = async (item) => {
  // 可以加载特定的历史对话
  ElMessage.info('加载历史对话功能开发中...')
}

onMounted(() => {
  loadHistory()
  
  // 添加欢迎消息
  messages.value.push({
    role: 'assistant',
    content: '你好！我是你的AI数学导师。你可以问我任何关于数学思维的问题，我会用苏格拉底式的方法引导你思考。',
    time: new Date()
  })
})
</script>

<style scoped>
.tutor-page {
  padding: 20px 0;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  font-size: 24px;
  color: #303133;
}

.chat-card {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message.user .message-content {
  background: #409eff;
  color: white;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}

.message.user .message-header {
  color: rgba(255, 255, 255, 0.8);
}

.role-name {
  font-weight: bold;
}

.time {
  opacity: 0.7;
}

.message-body {
  line-height: 1.6;
}

.message-body :deep(p) {
  margin: 8px 0;
}

.message-body :deep(ul), .message-body :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.message-body :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.input-area {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.hint {
  color: #909399;
  font-size: 12px;
}

.info-card, .tips-card {
  margin-bottom: 20px;
}

.empty-history {
  padding: 20px 0;
}

.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background 0.3s;
}

.history-item:hover {
  background: #f5f7fa;
}

.history-question {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.history-time {
  font-size: 12px;
  color: #909399;
}

.tips-list {
  padding-left: 20px;
}

.tips-list li {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}
</style>

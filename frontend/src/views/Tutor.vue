<template>
  <div class="tutor-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><ChatDotRound /></el-icon>
        AI问答
      </h2>
      <el-button
        type="primary"
        @click="startNewChat"
        class="new-chat-btn"
      >
        <el-icon><Plus /></el-icon>
        新建对话
      </el-button>
    </div>

    <el-row :gutter="24">
      <el-col :span="17">
        <el-card class="chat-card" shadow="never">
          <div class="chat-container" ref="chatContainer">
            <div v-for="(msg, index) in messages" :key="index"
                 :class="['message', msg.role]">
              <div class="message-avatar">
                <el-avatar 
                  :size="44" 
                  :icon="msg.role === 'user' ? UserFilled : School"
                  :class="msg.role"
                />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="role-name">{{ msg.role === 'user' ? '我' : 'AI助手' }}</span>
                  <span class="time">{{ formatTime(msg.time) }}</span>
                </div>
                <div class="message-body" v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>

            <div v-if="loading" class="message ai">
              <div class="message-avatar">
                <el-avatar :size="44" :icon="School" class="ai" />
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="role-name">AI助手</span>
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
              :rows="4"
              :placeholder="inputPlaceholder"
              @keyup.enter.ctrl="sendMessage"
              class="chat-input"
            />
            <div class="input-actions">
              <span class="hint">
                <el-icon><InfoFilled /></el-icon>
                Ctrl + Enter 发送
              </span>
              <el-button
                type="primary"
                @click="sendMessage"
                :loading="loading"
                :disabled="!inputMessage.trim()"
                size="large"
              >
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="7">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>对话历史</span>
            </div>
          </template>

          <div v-if="sessions.length === 0" class="empty-history">
            <el-empty description="暂无对话记录" :image-size="80" />
          </div>

          <div v-else class="history-list">
            <div
              v-for="session in sessions"
              :key="session.sessionId"
              class="history-item"
              :class="{ active: currentSessionId === session.sessionId }"
            >
              <div class="history-content" @click="loadSession(session.sessionId)">
                <div class="history-question">{{ session.sessionTitle || truncate(session.question, 30) }}</div>
                <div class="history-time">{{ formatDate(session.createdAt) }}</div>
              </div>
              <el-button
                type="danger"
                text
                size="small"
                class="delete-btn"
                @click.stop="deleteSession(session.sessionId)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card class="tips-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>提问技巧</span>
            </div>
          </template>

          <ul class="tips-list">
            <li>
              <el-icon><Check /></el-icon>
              <span>尽量描述具体的问题场景</span>
            </li>
            <li>
              <el-icon><Check /></el-icon>
              <span>说明你已经尝试过的方法</span>
            </li>
            <li>
              <el-icon><Check /></el-icon>
              <span>指出你不理解的具体步骤</span>
            </li>
            <li>
              <el-icon><Check /></el-icon>
              <span>可以要求AI举具体例子</span>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { tutorApi } from '../api'
import { UserFilled, School, Promotion, Plus, Delete, InfoFilled, Clock, Check } from '@element-plus/icons-vue'

const route = useRoute()
const messages = ref([])
const inputMessage = ref('')
const inputPlaceholder = ref('输入你的问题...')
const loading = ref(false)
const sessions = ref([])
const currentSessionId = ref(null)
const chatContainer = ref(null)
const noteContext = ref(null)

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

const startNewChat = () => {
  currentSessionId.value = null
  messages.value = []
  inputMessage.value = ''
  noteContext.value = null

  // 添加AI问候消息
  messages.value.push({
    role: 'assistant',
    content: '你好！我是你的AI学习伙伴 😊\n\n新对话已开始，有什么数学问题想要讨论吗？无论是概念理解、题目解答还是学习方法，我都很乐意帮助你！',
    time: new Date()
  })

  ElMessage.success('已开始新对话')
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
    const response = await tutorApi.ask(userId, question, currentSessionId.value)

    // 检查后端返回的错误
    if (response.data.error) {
      messages.value.push({
        role: 'assistant',
        content: response.data.error,
        time: new Date()
      })
      ElMessage.error(response.data.error)
      return
    }

    // 如果是新会话，保存sessionId
    if (!currentSessionId.value && response.data.sessionId) {
      currentSessionId.value = response.data.sessionId
    }

    messages.value.push({
      role: 'assistant',
      content: response.data.answer,
      time: new Date()
    })

    // 刷新会话列表
    loadSessions()
  } catch (error) {
    const errorMsg = error.response?.data?.error || error.message || '发送失败'
    messages.value.push({
      role: 'assistant',
      content: 'AI服务调用失败: ' + errorMsg,
      time: new Date()
    })
    ElMessage.error('发送失败: ' + errorMsg)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const loadSessions = async () => {
  try {
    const response = await tutorApi.getSessions(userId)
    sessions.value = response.data
  } catch (error) {
    console.error('加载会话列表失败', error)
  }
}

const loadSession = async (sessionId) => {
  try {
    currentSessionId.value = sessionId
    const response = await tutorApi.getSessionMessages(sessionId)

    // 将会话消息转换为前端格式
    messages.value = response.data.flatMap(msg => [
      {
        role: 'user',
        content: msg.question,
        time: new Date(msg.createdAt)
      },
      {
        role: 'assistant',
        content: msg.answer,
        time: new Date(msg.createdAt)
      }
    ])

    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载对话失败: ' + error.message)
  }
}

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条对话记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await tutorApi.deleteSession(sessionId)
    ElMessage.success('删除成功')
    
    // 如果删除的是当前正在查看的会话，清空当前会话
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
    }
    
    // 刷新会话列表
    loadSessions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

onMounted(() => {
  loadSessions()

  // 检查是否从笔记页面跳转过来
  if (route.query.from === 'notes' && route.query.noteContent) {
    const noteTitle = route.query.noteTitle || ''
    const noteContent = decodeURIComponent(route.query.noteContent)
    
    // 如果没有标题，从笔记内容中提取标题（第一行#开头的标题）
    let displayTitle = noteTitle
    if (!displayTitle || displayTitle.trim() === '') {
      const lines = noteContent.split('\n')
      for (const line of lines) {
        const trimmedLine = line.trim()
        if (trimmedLine.startsWith('# ')) {
          displayTitle = trimmedLine.substring(2).trim()
          break
        }
      }
      // 如果还是没找到，使用默认标题
      if (!displayTitle || displayTitle.trim() === '') {
        displayTitle = '笔记内容'
      }
    }
    
    noteContext.value = { title: displayTitle, content: noteContent }

    // 添加带有笔记上下文的欢迎消息
    messages.value.push({
      role: 'assistant',
      content: `你好！我是你的AI学习伙伴 😊\n\n我看到你刚刚整理了关于「${displayTitle}」的笔记，有什么关于这部分内容的问题想要深入探讨吗？我可以帮你：\n\n• 解释笔记中的难点概念\n• 补充相关的例题和练习\n• 解答你对知识点的疑问\n• 帮你建立知识之间的联系\n\n请随时提问！`,
      time: new Date()
    })

    // 设置输入框的placeholder提示
    inputPlaceholder.value = `关于「${displayTitle}」，你想问什么？`
  } else {
    // 添加普通欢迎消息
    messages.value.push({
      role: 'assistant',
      content: '你好！我是你的AI学习伙伴 😊 有什么数学问题都可以问我，我会用通俗易懂的方式帮你理解！',
      time: new Date()
    })
  }
})
</script>

<style scoped>
.tutor-page {
  padding: 0;
  height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-card {
  height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
  border: none;
  background: #fff;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #faf9f7;
  border-radius: 12px;
  margin-bottom: 20px;
}

.message {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar :deep(.el-avatar) {
  background: linear-gradient(135deg, #9db4c4 0%, #a8b5a0 100%);
  color: #fff;
}

.message-avatar :deep(.el-avatar.user) {
  background: linear-gradient(135deg, #8fa3b8 0%, #9db4c4 100%);
}

.message-content {
  max-width: 75%;
  background: #fff;
  padding: 16px 20px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0eeeb;
}

.message.user .message-content {
  background: linear-gradient(135deg, #8fa3b8 0%, #9db4c4 100%);
  color: white;
  border: none;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
}

.message.user .message-header {
  color: rgba(255, 255, 255, 0.9);
}

.role-name {
  font-weight: 600;
}

.time {
  opacity: 0.7;
  font-size: 12px;
}

.message-body {
  line-height: 1.7;
  font-size: 15px;
}

.message-body :deep(p) {
  margin: 10px 0;
}

.message-body :deep(ul), .message-body :deep(ol) {
  margin: 10px 0;
  padding-left: 24px;
}

.message-body :deep(code) {
  background: rgba(143, 163, 184, 0.15);
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  color: #6b8cae;
}

.message.user .message-body :deep(code) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.input-area {
  border-top: 1px solid #f0eeeb;
  padding-top: 20px;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  resize: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.hint {
  color: #999;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-card, .tips-card {
  margin-bottom: 20px;
  border: none;
}

.info-card :deep(.el-card__header),
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

.empty-history {
  padding: 24px 0;
}

.history-list {
  max-height: 280px;
  overflow-y: auto;
}

.history-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f0eeeb;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 8px;
  margin-bottom: 4px;
}

.history-item:hover {
  background: #faf9f7;
}

.history-item.active {
  background: rgba(143, 163, 184, 0.12);
  border-left: 3px solid #8fa3b8;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.history-question {
  font-size: 14px;
  color: #3a3a3a;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.history-time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.history-item:hover .delete-btn {
  opacity: 1;
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

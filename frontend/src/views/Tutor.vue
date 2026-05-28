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
            <!-- 图片预览区域 -->
            <div v-if="selectedImages.length > 0" class="image-preview-area">
              <div v-for="(img, index) in selectedImages" :key="index" class="image-preview-item">
                <img :src="img.preview" alt="预览" />
                <div class="remove-image" @click="removeImage(index)">
                  <el-icon><Close /></el-icon>
                </div>
              </div>
            </div>

            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="4"
              :placeholder="inputPlaceholder"
              @keyup.enter.ctrl="sendMessage"
              class="chat-input"
            />
            <div class="input-actions">
              <div class="left-actions">
                <el-upload
                  ref="uploadRef"
                  action=""
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleImageChange"
                  :limit="3"
                  accept="image/jpeg,image/png,image/gif"
                  class="image-upload"
                >
                  <el-button type="info" text size="small">
                    <el-icon><Picture /></el-icon>
                    上传图片
                  </el-button>
                </el-upload>
                <span class="hint">
                  <el-icon><InfoFilled /></el-icon>
                  Ctrl + Enter 发送
                </span>
              </div>
              <el-button
                type="primary"
                @click="sendMessage"
                :loading="loading"
                :disabled="!inputMessage.trim() && selectedImages.length === 0"
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
import { UserFilled, School, Promotion, Plus, Delete, InfoFilled, Clock, Check, Picture, Close } from '@element-plus/icons-vue'

const route = useRoute()
const messages = ref([])
const inputMessage = ref('')
const inputPlaceholder = ref('输入你的问题...')
const loading = ref(false)
const sessions = ref([])
const currentSessionId = ref(null)
const chatContainer = ref(null)
const noteContext = ref(null)
const selectedImages = ref([])
const uploadRef = ref(null)

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
  selectedImages.value = []

  // 添加AI问候消息
  messages.value.push({
    role: 'assistant',
    content: '你好！我是你的AI学习伙伴 😊\n\n新对话已开始，有什么数学问题想要讨论吗？无论是概念理解、题目解答还是学习方法，我都很乐意帮助你！',
    time: new Date()
  })

  ElMessage.success('已开始新对话')
}

const handleImageChange = (file) => {
  if (selectedImages.value.length >= 3) {
    ElMessage.warning('最多只能上传3张图片')
    return
  }

  // 检查文件类型
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }

  // 检查文件大小（5MB）
  const isLt5M = file.raw.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  // 创建预览URL
  const preview = URL.createObjectURL(file.raw)
  selectedImages.value.push({
    file: file.raw,
    preview: preview
  })
}

const removeImage = (index) => {
  selectedImages.value.splice(index, 1)
}

const uploadImages = async () => {
  const uploadedUrls = []
  for (const img of selectedImages.value) {
    try {
      const formData = new FormData()
      formData.append('file', img.file)
      const response = await tutorApi.uploadImage(formData)
      uploadedUrls.push(response.data.url)
    } catch (error) {
      console.error('图片上传失败', error)
      ElMessage.error('图片上传失败')
    }
  }
  return uploadedUrls
}

const sendMessage = async () => {
  const question = inputMessage.value.trim()
  if ((!question && selectedImages.value.length === 0) || loading.value) return

  // 上传图片
  let imageUrls = []
  if (selectedImages.value.length > 0) {
    imageUrls = await uploadImages()
  }

  // 构建消息内容
  let messageContent = question
  if (imageUrls.length > 0) {
    messageContent += '\n\n[图片]'
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: messageContent,
    images: selectedImages.value.map(img => img.preview),
    time: new Date()
  })

  inputMessage.value = ''
  selectedImages.value = []
  loading.value = true
  scrollToBottom()

  try {
    const response = await tutorApi.ask(userId, question, currentSessionId.value, imageUrls)

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
    ElMessage.error('发送失败: ' + error.message)
    messages.value.pop()
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
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 28px;
  color: #1a202c;
  font-weight: 700;
}

.page-title .el-icon {
  color: #667eea;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-card {
  height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 16px;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  margin-bottom: 24px;
}

.message {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.message-avatar :deep(.el-avatar.user) {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.3);
}

.message-content {
  max-width: 75%;
  background: #fff;
  padding: 18px 22px;
  border-radius: 18px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
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
  line-height: 1.8;
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
  background: rgba(102, 126, 234, 0.1);
  padding: 3px 8px;
  border-radius: 6px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  color: #667eea;
}

.message.user .message-body :deep(code) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.input-area {
  border-top: 1px solid #e2e8f0;
  padding-top: 24px;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 14px;
  padding: 18px;
  font-size: 15px;
  resize: none;
  border-color: #e2e8f0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18px;
}

.left-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.hint {
  color: #a0aec0;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.image-upload :deep(.el-upload) {
  display: inline-block;
}

.image-preview-area {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.image-preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: rgba(245, 101, 101, 0.8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.remove-image:hover {
  background: rgba(245, 101, 101, 1);
  transform: scale(1.1);
}

.info-card, .tips-card {
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
}

.info-card :deep(.el-card__header),
.tips-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-bottom: 1px solid #e2e8f0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #1a202c;
  font-size: 16px;
}

.empty-history {
  padding: 28px 0;
}

.history-list {
  max-height: 280px;
  overflow-y: auto;
}

.history-item {
  padding: 16px 18px;
  border-bottom: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 10px;
  margin-bottom: 6px;
}

.history-item:hover {
  background: #f7fafc;
}

.history-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-left: 3px solid #667eea;
}

.history-content {
  flex: 1;
  min-width: 0;
}

.history-question {
  font-size: 14px;
  color: #2d3748;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.history-time {
  font-size: 12px;
  color: #a0aec0;
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
  padding: 14px 0;
  color: #4a5568;
  font-size: 14px;
  line-height: 1.6;
  border-bottom: 1px solid #e2e8f0;
}

.tips-list li:last-child {
  border-bottom: none;
}

.tips-list li :deep(.el-icon) {
  color: #48bb78;
  margin-top: 2px;
  flex-shrink: 0;
}
</style>

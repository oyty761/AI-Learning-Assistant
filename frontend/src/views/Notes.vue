<template>
  <div class="notes-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Document /></el-icon>
        智能笔记
      </h2>
      <p class="page-desc">上传学习材料，AI自动生成结构化笔记</p>
    </div>

    <el-row :gutter="24">
      <el-col :span="11">
        <el-card class="upload-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Upload /></el-icon>
              <span>上传学习材料</span>
            </div>
          </template>

          <el-form :model="form" label-position="top" class="notes-form">
            <el-form-item label="原始内容">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="粘贴PPT/PDF的文本内容，或手动输入"
                class="content-input"
              />
            </el-form-item>

            <el-form-item>
              <el-upload
                ref="uploadRef"
                class="upload-demo"
                drag
                action="/api/notes/upload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :on-remove="handleUploadRemove"
                :file-list="fileList"
                accept=".pdf,.ppt,.pptx,.jpg,.jpeg,.png"
              >
                <el-icon class="el-icon--upload" :size="48"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽文件到此处或 <em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 PDF、PPT、图片格式，文件不超过10MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <el-divider class="section-divider" />

            <el-form-item label="核心主题（可选）">
              <el-input
                v-model="form.theme"
                placeholder="留空将由AI自动提炼主题"
                class="theme-input"
              />
            </el-form-item>

            <el-form-item label="重点概念（可选）">
              <el-input
                v-model="form.concepts"
                type="textarea"
                :rows="3"
                placeholder="留空将由AI自动识别重点概念"
                class="concepts-input"
              />
            </el-form-item>

            <el-form-item class="submit-item">
              <el-button 
                type="primary" 
                @click="generateNotes"
                :loading="loading"
                size="large"
                class="generate-btn"
              >
                <el-icon><MagicStick /></el-icon>
                生成结构化笔记
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="13">
        <el-card class="preview-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon><View /></el-icon>
                <span>笔记预览</span>
              </div>
              <div v-if="generatedNote" class="header-actions">
                <el-button type="primary" text @click="exportMarkdown" class="action-btn">
                  <el-icon><Download /></el-icon>
                  导出Markdown
                </el-button>
                <el-button type="success" text @click="goToTutor" class="action-btn">
                  <el-icon><ChatDotRound /></el-icon>
                  深入问答
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="generatedNote" class="note-content" v-html="renderedContent"></div>
          <div v-else class="empty-state">
            <el-empty description="生成的笔记将在这里显示" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="history-card" v-if="notes.length > 0" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>历史笔记</span>
        </div>
      </template>

      <el-table :data="notes" style="width: 100%" class="history-table">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" text @click="viewNote(scope.row)">
              查看
            </el-button>
            <el-button type="danger" text @click="deleteNote(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import { notesApi } from '../api'
import { 
  Document, 
  UploadFilled, 
  MagicStick, 
  Download, 
  ChatDotRound,
  Upload,
  View,
  Clock
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const generatedNote = ref(null)
const notes = ref([])
const uploadRef = ref(null)
const fileList = ref([])
const uploadedText = ref('')

const form = ref({
  theme: '',
  concepts: '',
  content: ''
})

const userId = 'user001'

const renderedContent = computed(() => {
  if (!generatedNote.value?.content) return ''
  return marked(generatedNote.value.content)
})

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleUploadSuccess = (response) => {
  if (response.text && response.text.trim()) {
    // 保存上传文件提取的文本，用于删除时清除
    uploadedText.value = response.text

    // 如果返回了识别的文本，自动填充到内容区域
    if (!form.value.content) {
      form.value.content = response.text
    } else {
      form.value.content += '\n\n' + response.text
    }
    if (response.text.includes('OCR识别暂时不可用') || response.text.includes('未配置') ||
        response.text.includes('提取失败') || response.text.includes('手动输入')) {
      ElMessage.warning('文件已上传，但内容识别需要手动输入')
    } else {
      ElMessage.success('文件上传并识别成功')
    }
  } else {
    ElMessage.success('文件上传成功')
  }
  form.value.sourceFile = response.fileName
}

const handleUploadRemove = () => {
  // 删除文件时，清除原始内容框中该文件的内容
  if (uploadedText.value && form.value.content) {
    form.value.content = form.value.content.replace(uploadedText.value, '').trim()
    // 清理多余的换行符
    form.value.content = form.value.content.replace(/\n{3,}/g, '\n\n')
  }
  uploadedText.value = ''
  form.value.sourceFile = null
  ElMessage.info('文件已删除，相关内容已清除')
}

const handleUploadError = (error) => {
  console.error('上传错误:', error)
  let errorMsg = '文件上传失败，请检查网络连接或文件格式'

  // 处理后端返回的错误对象
  if (error && error.response && error.response.data) {
    const data = error.response.data
    if (data.text) {
      errorMsg = data.text
    } else if (data.message) {
      errorMsg = data.message
    } else if (data.error) {
      errorMsg = data.error
    }
  } else if (error && error.message) {
    errorMsg = error.message
  } else if (typeof error === 'string') {
    errorMsg = error
  }

  ElMessage.error(errorMsg)
}

const generateNotes = async () => {
  if (!form.value.content || !form.value.content.trim()) {
    ElMessage.warning('请填写原始内容或上传文件')
    return
  }

  loading.value = true
  try {
    const response = await notesApi.generateNotes({
      userId,
      theme: form.value.theme,
      concepts: form.value.concepts,
      content: form.value.content,
      sourceFile: form.value.sourceFile
    })
    generatedNote.value = response.data
    ElMessage.success('笔记生成成功')
    loadNotes()
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadNotes = async () => {
  try {
    const response = await notesApi.getNotes(userId)
    notes.value = response.data
  } catch (error) {
    console.error('加载笔记失败', error)
  }
}

const viewNote = (note) => {
  generatedNote.value = note
}

const deleteNote = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条笔记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await notesApi.deleteNote(id)
    ElMessage.success('删除成功')
    loadNotes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const exportMarkdown = () => {
  if (!generatedNote.value?.content) return
  
  const blob = new Blob([generatedNote.value.content], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${generatedNote.value.title || '笔记'}.md`
  a.click()
  URL.revokeObjectURL(url)
}

const goToTutor = () => {
  if (!generatedNote.value?.content) {
    ElMessage.warning('请先生成笔记')
    return
  }
  // 将笔记内容传递到问答页面
  router.push({
    path: '/tutor',
    query: {
      from: 'notes',
      noteTitle: generatedNote.value.title || '',
      noteContent: encodeURIComponent(generatedNote.value.content)
    }
  })
}

onMounted(() => {
  loadNotes()
})
</script>

<style scoped>
.notes-page {
  padding: 0;
}

.page-header {
  margin-bottom: 28px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 10px;
  font-size: 28px;
  color: #1a202c;
  font-weight: 700;
}

.page-title .el-icon {
  color: #38b2ac;
}

.page-desc {
  color: #718096;
  font-size: 15px;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.card-header .header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #1a202c;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.upload-card :deep(.el-card__header),
.preview-card :deep(.el-card__header),
.history-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-bottom: 1px solid #e2e8f0;
}

.upload-card, .preview-card {
  margin-bottom: 24px;
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.notes-form {
  padding: 8px 0;
}

.notes-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2d3748;
  font-size: 15px;
  padding-bottom: 8px;
}

.content-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  line-height: 1.7;
  color: #2d3748;
}

.upload-demo :deep(.el-upload-dragger) {
  border-radius: 16px;
  border-color: #e2e8f0;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  padding: 40px;
  transition: all 0.3s ease;
}

.upload-demo :deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: linear-gradient(135deg, #edf2f7 0%, #e2e8f0 100%);
}

.upload-demo :deep(.el-icon--upload) {
  color: #667eea;
  margin-bottom: 16px;
}

.upload-demo :deep(.el-upload__text) {
  color: #4a5568;
  font-size: 15px;
}

.upload-demo :deep(.el-upload__text em) {
  color: #667eea;
  font-style: normal;
  font-weight: 600;
}

.upload-demo :deep(.el-upload__tip) {
  color: #999;
  font-size: 13px;
  margin-top: 8px;
}

.section-divider {
  margin: 24px 0;
  border-color: #f0eeeb;
}

.theme-input :deep(.el-input__wrapper),
.concepts-input :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.submit-item {
  margin-top: 24px;
  margin-bottom: 0;
}

.generate-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 10px;
}

.note-content {
  padding: 24px;
  background: #faf9f7;
  border-radius: 12px;
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
  line-height: 1.8;
}

.note-content :deep(h1) {
  font-size: 26px;
  margin-bottom: 20px;
  color: #3a3a3a;
  font-weight: 600;
  border-bottom: 2px solid #e0ddd8;
  padding-bottom: 12px;
}

.note-content :deep(h2) {
  font-size: 20px;
  margin: 24px 0 16px;
  color: #3a3a3a;
  font-weight: 600;
}

.note-content :deep(h3) {
  font-size: 17px;
  margin: 20px 0 12px;
  color: #555;
  font-weight: 600;
}

.note-content :deep(p) {
  margin: 14px 0;
  line-height: 1.9;
  color: #555;
  font-size: 15px;
}

.note-content :deep(ul), .note-content :deep(ol) {
  margin: 14px 0;
  padding-left: 28px;
}

.note-content :deep(li) {
  margin: 10px 0;
  color: #555;
  line-height: 1.8;
}

.note-content :deep(code) {
  background: rgba(143, 163, 184, 0.15);
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  color: #6b8cae;
}

.note-content :deep(pre) {
  background: #fff;
  padding: 16px;
  border-radius: 10px;
  overflow-x: auto;
  border: 1px solid #f0eeeb;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.history-card {
  margin-top: 24px;
}

.history-table :deep(.el-table__header th) {
  background: #faf9f7;
  color: #3a3a3a;
  font-weight: 600;
}

.history-table :deep(.el-table__row) {
  transition: background 0.3s ease;
}

.history-table :deep(.el-table__row:hover) {
  background: #faf9f7;
}
</style>

<template>
  <div class="notes-page">
    <h2 class="page-title">
      <el-icon><Document /></el-icon>
      笔记整理官
    </h2>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="upload-card">
          <template #header>
            <div class="card-header">
              <span>上传学习材料</span>
            </div>
          </template>

          <el-form :model="form" label-position="top">
            <el-form-item label="核心主题">
              <el-input 
                v-model="form.theme" 
                placeholder="例如：线性代数 - 特征值与特征向量"
              />
            </el-form-item>

            <el-form-item label="重点概念">
              <el-input 
                v-model="form.concepts" 
                type="textarea" 
                :rows="2"
                placeholder="你想重点梳理哪几个概念的关系？"
              />
            </el-form-item>

            <el-form-item label="原始内容">
              <el-input 
                v-model="form.content" 
                type="textarea" 
                :rows="6"
                placeholder="粘贴PPT/PDF的文本内容，或手动输入"
              />
            </el-form-item>

            <el-form-item>
              <el-upload
                class="upload-demo"
                drag
                action="/api/notes/upload"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                accept=".pdf,.ppt,.pptx,.jpg,.jpeg,.png"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
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

            <el-form-item>
              <el-button 
                type="primary" 
                @click="generateNotes"
                :loading="loading"
                size="large"
              >
                <el-icon><MagicStick /></el-icon>
                生成结构化笔记
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="preview-card">
          <template #header>
            <div class="card-header">
              <span>笔记预览</span>
              <div v-if="generatedNote">
                <el-button type="primary" text @click="exportMarkdown">
                  <el-icon><Download /></el-icon>
                  导出Markdown
                </el-button>
                <el-button type="success" text @click="goToTutor">
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

    <el-card class="history-card" v-if="notes.length > 0">
      <template #header>
        <div class="card-header">
          <span>历史笔记</span>
        </div>
      </template>

      <el-table :data="notes" style="width: 100%">
        <el-table-column prop="title" label="标题" />
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

const router = useRouter()
const loading = ref(false)
const generatedNote = ref(null)
const notes = ref([])

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
  ElMessage.success('文件上传成功')
  form.value.sourceFile = response.fileName
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

const generateNotes = async () => {
  if (!form.value.theme || !form.value.content) {
    ElMessage.warning('请填写主题和内容')
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
  router.push('/tutor')
}

onMounted(() => {
  loadNotes()
})
</script>

<style scoped>
.notes-page {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-card, .preview-card {
  margin-bottom: 20px;
}

.note-content {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
}

.note-content :deep(h1) {
  font-size: 24px;
  margin-bottom: 16px;
  color: #303133;
}

.note-content :deep(h2) {
  font-size: 20px;
  margin: 20px 0 12px;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.note-content :deep(p) {
  margin: 12px 0;
  line-height: 1.8;
  color: #606266;
}

.note-content :deep(ul), .note-content :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.note-content :deep(li) {
  margin: 8px 0;
  color: #606266;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.history-card {
  margin-top: 20px;
}
</style>

<template>
  <div class="diagnose-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Search /></el-icon>
        错题诊断
      </h2>
      <p class="page-desc">上传题目和解答，AI帮你分析错误原因</p>
    </div>

    <el-row :gutter="24">
      <el-col :span="11">
        <el-card class="upload-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Upload /></el-icon>
              <span>上传题目和解答</span>
            </div>
          </template>

          <el-form :model="form" label-position="top" class="diagnose-form">
            <div class="upload-section">
              <el-form-item label="题目图片">
                <el-upload
                  class="upload-demo"
                  drag
                  action="/api/diagnose/upload"
                  :on-success="handleQuestionUpload"
                  :on-error="handleUploadError"
                  accept=".jpg,.jpeg,.png"
                >
                  <el-icon class="el-icon--upload" :size="48"><upload-filled /></el-icon>
                  <div class="el-upload__text">
                    拖拽题目图片到此处或 <em>点击上传</em>
                  </div>
                </el-upload>
              </el-form-item>

              <el-form-item label="题目内容（可编辑）">
                <el-input 
                  v-model="form.questionText" 
                  type="textarea" 
                  :rows="4"
                  placeholder="OCR识别的题目内容，可手动修正"
                  class="content-input"
                />
              </el-form-item>
            </div>

            <el-divider class="section-divider" />

            <div class="upload-section">
              <el-form-item label="解答图片">
                <el-upload
                  class="upload-demo"
                  drag
                  action="/api/diagnose/upload"
                  :on-success="handleAnswerUpload"
                  :on-error="handleUploadError"
                  accept=".jpg,.jpeg,.png"
                >
                  <el-icon class="el-icon--upload" :size="48"><upload-filled /></el-icon>
                  <div class="el-upload__text">
                    拖拽解答图片到此处或 <em>点击上传</em>
                  </div>
                </el-upload>
              </el-form-item>

              <el-form-item label="你的解答（可编辑）">
                <el-input 
                  v-model="form.userAnswer" 
                  type="textarea" 
                  :rows="5"
                  placeholder="OCR识别的解答内容，可手动修正"
                  class="content-input"
                />
              </el-form-item>
            </div>

            <el-form-item class="submit-item">
              <el-button 
                type="primary" 
                @click="analyze"
                :loading="loading"
                size="large"
                class="analyze-btn"
                :disabled="!form.questionText || !form.userAnswer"
              >
                <el-icon><Search /></el-icon>
                开始诊断
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="13">
        <el-card class="result-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon><DocumentChecked /></el-icon>
                <span>诊断结果</span>
              </div>
              <el-button 
                v-if="diagnosisResult" 
                type="success" 
                text 
                @click="goToExam"
                class="go-exam-btn"
              >
                <el-icon><Edit /></el-icon>
                去练习
              </el-button>
            </div>
          </template>

          <div v-if="diagnosisResult" class="diagnosis-content">
            <!-- 错误类型标签 -->
            <div class="error-type-wrapper">
              <div class="error-type-tag" :class="getErrorTypeClass(diagnosisResult.errorType)">
                {{ diagnosisResult.errorType || '未知类型' }}
              </div>
            </div>

            <!-- 解答正确时的成功提示 -->
            <div v-if="diagnosisResult.errorType === '解答正确'" class="success-warning">
              <el-alert
                title="🎉 恭喜你，解答正确！"
                type="success"
                :closable="false"
                show-icon
              >
                <p>你的解题思路和答案都是正确的，继续保持！</p>
              </el-alert>
            </div>

            <!-- 解析失败时的特殊提示 -->
            <div v-if="diagnosisResult.errorType === '解析失败'" class="error-warning">
              <el-alert
                title="诊断结果解析异常"
                type="warning"
                :closable="false"
                show-icon
              >
                <p>AI返回的结果格式不正确，但已尽力提取可用信息。</p>
                <p class="suggestion">建议：请检查题目和解答是否输入完整，然后重新诊断。</p>
              </el-alert>
            </div>

            <div class="result-sections">
              <div class="section">
                <h4>
                  <el-icon><Collection /></el-icon>
                  涉及知识点
                </h4>
                <div class="section-content">{{ diagnosisResult.knowledgePoints || '暂无识别结果' }}</div>
              </div>

              <div class="section">
                <h4>
                  <el-icon><ChatDotSquare /></el-icon>
                  诊断反馈
                </h4>
                <div class="section-content">{{ diagnosisResult.feedback || '暂无反馈' }}</div>
              </div>

              <!-- 只有解答不正确时才显示提示性问题 -->
              <div class="section hint-section" v-if="diagnosisResult.hintQuestion && diagnosisResult.errorType !== '解答正确'">
                <h4>
                  <el-icon><QuestionFilled /></el-icon>
                  提示性问题
                </h4>
                <div class="section-content">{{ diagnosisResult.hintQuestion }}</div>
              </div>

              <!-- 只有解答不正确时才显示相似例题 -->
              <div class="section" v-if="diagnosisResult.similarExample && diagnosisResult.similarExample !== '暂无' && diagnosisResult.errorType !== '解答正确'">
                <h4>
                  <el-icon><CopyDocument /></el-icon>
                  相似例题
                </h4>
                <div class="example-box">
                  {{ diagnosisResult.similarExample }}
                </div>
              </div>

              <el-divider v-if="diagnosisResult.fullSolution && diagnosisResult.fullSolution !== '暂无'" class="result-divider" />

              <div class="section" v-if="diagnosisResult.fullSolution && diagnosisResult.fullSolution !== '暂无'">
                <h4>
                  <el-icon><View /></el-icon>
                  {{ diagnosisResult.errorType === '解答正确' ? '参考答案' : '完整解析' }}
                </h4>
                <el-collapse class="solution-collapse">
                  <el-collapse-item :title="diagnosisResult.errorType === '解答正确' ? '点击查看参考答案' : '点击查看完整解答'" name="1">
                    <div class="solution-content">
                      {{ diagnosisResult.fullSolution }}
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            <el-empty 
              description="上传题目和解答后，AI将为你诊断错误" 
              :image-size="120"
            >
              <template #image>
                <div class="empty-icon">
                  <el-icon :size="64"><Search /></el-icon>
                </div>
              </template>
            </el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="history-card" v-if="records.length > 0" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>诊断历史</span>
        </div>
      </template>

      <el-table :data="records" style="width: 100%" class="history-table">
        <el-table-column prop="errorType" label="错误类型" width="130">
          <template #default="scope">
            <el-tag :type="getErrorTypeTagType(scope.row.errorType)" class="error-tag">
              {{ scope.row.errorType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="knowledgePoints" label="知识点" min-width="200" />
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" text @click="viewRecord(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="profile-card" v-if="errorProfile.length > 0" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><FolderOpened /></el-icon>
          <span>错误档案</span>
        </div>
      </template>

      <el-table :data="errorProfile" style="width: 100%" class="profile-table">
        <el-table-column prop="knowledgePoint" label="知识点" min-width="200" />
        <el-table-column prop="errorType" label="错误类型" width="130">
          <template #default="scope">
            <el-tag :type="getErrorTypeTagType(scope.row.errorType)" class="error-tag">
              {{ scope.row.errorType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误次数" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.errorCount >= 2 ? 'danger' : 'warning'" class="count-tag">
              {{ scope.row.errorCount }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { diagnoseApi } from '../api'
import { 
  Search, 
  UploadFilled, 
  QuestionFilled, 
  Edit, 
  Upload, 
  DocumentChecked, 
  Collection, 
  ChatDotSquare, 
  CopyDocument, 
  View, 
  Clock,
  FolderOpened 
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const diagnosisResult = ref(null)
const records = ref([])
const errorProfile = ref([])

const form = ref({
  questionText: '',
  userAnswer: '',
  questionImage: '',
  answerImage: ''
})

const userId = 'user001'

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getErrorTypeClass = (type) => {
  const map = {
    '概念误解型': 'concept',
    '计算疏忽型': 'calculation',
    '迁移困难型': 'transfer',
    '逻辑断层型': 'logic',
    '无错误': 'correct',
    '解答正确': 'correct'
  }
  return map[type] || 'unknown'
}

const getErrorTypeTagType = (type) => {
  const map = {
    '概念误解型': 'danger',
    '计算疏忽型': 'warning',
    '迁移困难型': 'primary',
    '逻辑断层型': 'info',
    '无错误': 'success',
    '解答正确': 'success'
  }
  return map[type] || 'info'
}

const handleQuestionUpload = (response) => {
  if (response.text && response.text.trim()) {
    form.value.questionText = response.text
    if (response.text.includes('OCR识别暂时不可用') || response.text.includes('未配置')) {
      ElMessage.warning('图片识别未完成，请手动输入题目内容')
    } else {
      ElMessage.success('题目识别成功')
    }
  } else {
    ElMessage.warning('未能识别图片内容，请手动输入')
  }
}

const handleAnswerUpload = (response) => {
  if (response.text && response.text.trim()) {
    form.value.userAnswer = response.text
    if (response.text.includes('OCR识别暂时不可用') || response.text.includes('未配置')) {
      ElMessage.warning('图片识别未完成，请手动输入解答内容')
    } else {
      ElMessage.success('解答识别成功')
    }
  } else {
    ElMessage.warning('未能识别图片内容，请手动输入')
  }
}

const handleUploadError = (error) => {
  console.error('上传错误:', error)
  let errorMsg = '上传失败，请检查网络连接或文件格式'

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

const analyze = async () => {
  if (!form.value.questionText || !form.value.userAnswer) {
    ElMessage.warning('请填写题目和解答内容')
    return
  }

  loading.value = true
  try {
    const response = await diagnoseApi.analyze({
      userId,
      questionText: form.value.questionText,
      userAnswer: form.value.userAnswer,
      questionImage: form.value.questionImage,
      answerImage: form.value.answerImage
    })
    diagnosisResult.value = response.data
    ElMessage.success('诊断完成')
    loadData()
  } catch (error) {
    ElMessage.error('诊断失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadData = async () => {
  try {
    const [recordsRes, profileRes] = await Promise.all([
      diagnoseApi.getRecords(userId),
      diagnoseApi.getProfile(userId)
    ])
    records.value = recordsRes.data
    errorProfile.value = profileRes.data
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

const viewRecord = (record) => {
  diagnosisResult.value = record
}

const goToExam = () => {
  router.push('/exam')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.diagnose-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 8px;
  font-size: 26px;
  color: #3a3a3a;
  font-weight: 600;
}

.page-desc {
  color: #888;
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
  gap: 8px;
  font-weight: 500;
  color: #3a3a3a;
}

.upload-card :deep(.el-card__header),
.result-card :deep(.el-card__header),
.history-card :deep(.el-card__header),
.profile-card :deep(.el-card__header) {
  background: #faf9f7;
  border-bottom: 1px solid #f0eeeb;
}

.upload-card, .result-card {
  margin-bottom: 24px;
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.diagnose-form {
  padding: 8px 0;
}

.upload-section {
  margin-bottom: 8px;
}

.section-divider {
  margin: 24px 0;
  border-color: #f0eeeb;
}

.diagnose-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #3a3a3a;
  font-size: 15px;
  padding-bottom: 8px;
}

.content-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 14px;
  font-size: 15px;
  line-height: 1.7;
}

.upload-demo :deep(.el-upload-dragger) {
  border-radius: 12px;
  border-color: #e0ddd8;
  background: #faf9f7;
  padding: 32px;
  transition: all 0.3s ease;
}

.upload-demo :deep(.el-upload-dragger:hover) {
  border-color: #c4b5a0;
  background: #f5f3f0;
}

.upload-demo :deep(.el-icon--upload) {
  color: #a8b5a0;
  margin-bottom: 12px;
}

.upload-demo :deep(.el-upload__text) {
  color: #666;
  font-size: 14px;
}

.upload-demo :deep(.el-upload__text em) {
  color: #8fa3b8;
  font-style: normal;
  font-weight: 500;
}

.submit-item {
  margin-top: 24px;
  margin-bottom: 0;
}

.analyze-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 10px;
}

.diagnosis-content {
  padding: 8px;
}

.error-type-wrapper {
  margin-bottom: 20px;
}

.error-type-tag {
  display: inline-block;
  padding: 10px 20px;
  border-radius: 24px;
  font-weight: 600;
  font-size: 15px;
}

/* 莫兰迪色系错误类型标签 */
.error-type-tag.concept {
  background: rgba(184, 138, 138, 0.15);
  color: #b88a8a;
}

.error-type-tag.calculation {
  background: rgba(196, 167, 125, 0.15);
  color: #c4a77d;
}

.error-type-tag.transfer {
  background: rgba(107, 140, 174, 0.15);
  color: #6b8cae;
}

.error-type-tag.logic {
  background: rgba(143, 163, 184, 0.15);
  color: #8fa3b8;
}

.error-type-tag.correct {
  background: rgba(122, 158, 126, 0.15);
  color: #7a9e7e;
}

.error-type-tag.unknown {
  background: rgba(160, 160, 160, 0.15);
  color: #888;
}

.error-warning {
  margin-bottom: 24px;
}

.error-warning .suggestion {
  margin-top: 10px;
  color: #c4a77d;
  font-weight: 500;
}

.success-warning {
  margin-bottom: 24px;
}

.success-warning :deep(.el-alert__title) {
  font-weight: 600;
}

.result-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section {
  background: #faf9f7;
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #c4b5a0;
}

.section h4 {
  font-size: 16px;
  color: #3a3a3a;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.section h4 :deep(.el-icon) {
  color: #8fa3b8;
}

.section-content {
  color: #555;
  line-height: 1.8;
  font-size: 15px;
}

.hint-section {
  border-left-color: #6b8cae;
  background: rgba(107, 140, 174, 0.06);
}

.hint-section h4 :deep(.el-icon) {
  color: #6b8cae;
}

.example-box {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  font-family: 'Fira Code', monospace;
  white-space: pre-wrap;
  border: 1px solid #f0eeeb;
  font-size: 14px;
  line-height: 1.8;
  color: #555;
}

.result-divider {
  margin: 24px 0;
  border-color: #f0eeeb;
}

.solution-collapse {
  border: none;
  border-radius: 10px;
  overflow: hidden;
}

.solution-collapse :deep(.el-collapse-item__header) {
  background: #faf9f7;
  padding: 16px 20px;
  font-weight: 500;
  color: #3a3a3a;
  border: none;
}

.solution-collapse :deep(.el-collapse-item__content) {
  padding: 0;
}

.solution-content {
  background: #fff;
  padding: 20px;
  border-radius: 0 0 10px 10px;
  white-space: pre-wrap;
  line-height: 1.9;
  font-size: 15px;
  color: #555;
  border: 1px solid #f0eeeb;
  border-top: none;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-icon {
  color: #c4b5a0;
  margin-bottom: 16px;
}

.history-card, .profile-card {
  margin-top: 24px;
}

.history-table :deep(.el-table__header th),
.profile-table :deep(.el-table__header th) {
  background: #faf9f7;
  color: #3a3a3a;
  font-weight: 600;
}

.history-table :deep(.el-table__row),
.profile-table :deep(.el-table__row) {
  transition: background 0.3s ease;
}

.history-table :deep(.el-table__row:hover),
.profile-table :deep(.el-table__row:hover) {
  background: #faf9f7;
}

.error-tag {
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 4px;
}

.count-tag {
  font-weight: 600;
}

.go-exam-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>

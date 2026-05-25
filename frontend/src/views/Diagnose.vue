<template>
  <div class="diagnose-page">
    <h2 class="page-title">
      <el-icon><Search /></el-icon>
      解题诊断师
    </h2>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="upload-card">
          <template #header>
            <div class="card-header">
              <span>上传题目和解答</span>
            </div>
          </template>

          <el-form :model="form" label-position="top">
            <el-form-item label="题目图片">
              <el-upload
                class="upload-demo"
                drag
                action="/api/diagnose/upload"
                :on-success="handleQuestionUpload"
                :on-error="handleUploadError"
                accept=".jpg,.jpeg,.png"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
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
              />
            </el-form-item>

            <el-form-item label="解答图片">
              <el-upload
                class="upload-demo"
                drag
                action="/api/diagnose/upload"
                :on-success="handleAnswerUpload"
                :on-error="handleUploadError"
                accept=".jpg,.jpeg,.png"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽解答图片到此处或 <em>点击上传</em>
                </div>
              </el-upload>
            </el-form-item>

            <el-form-item label="你的解答（可编辑）">
              <el-input 
                v-model="form.userAnswer" 
                type="textarea" 
                :rows="6"
                placeholder="OCR识别的解答内容，可手动修正"
              />
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                @click="analyze"
                :loading="loading"
                size="large"
                :disabled="!form.questionText || !form.userAnswer"
              >
                <el-icon><Search /></el-icon>
                开始诊断
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>诊断结果</span>
              <el-button 
                v-if="diagnosisResult" 
                type="success" 
                text 
                @click="goToExam"
              >
                <el-icon><Edit /></el-icon>
                去练习
              </el-button>
            </div>
          </template>

          <div v-if="diagnosisResult" class="diagnosis-content">
            <div class="error-type-tag" :class="getErrorTypeClass(diagnosisResult.errorType)">
              {{ diagnosisResult.errorType }}
            </div>

            <div class="section">
              <h4>涉及知识点</h4>
              <p>{{ diagnosisResult.knowledgePoints }}</p>
            </div>

            <div class="section">
              <h4>诊断反馈</h4>
              <p>{{ diagnosisResult.feedback }}</p>
            </div>

            <div class="section hint-section">
              <h4>
                <el-icon><QuestionFilled /></el-icon>
                提示性问题
              </h4>
              <p>{{ diagnosisResult.hintQuestion }}</p>
            </div>

            <div class="section">
              <h4>相似例题</h4>
              <div class="example-box">
                {{ diagnosisResult.similarExample }}
              </div>
            </div>

            <el-divider />

            <div class="section">
              <h4>完整解析</h4>
              <el-collapse>
                <el-collapse-item title="点击查看完整解答">
                  <div class="solution-content">
                    {{ diagnosisResult.fullSolution }}
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>

          <div v-else class="empty-state">
            <el-empty description="上传题目和解答后，AI将为你诊断错误" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="history-card" v-if="records.length > 0">
      <template #header>
        <div class="card-header">
          <span>诊断历史</span>
        </div>
      </template>

      <el-table :data="records" style="width: 100%">
        <el-table-column prop="errorType" label="错误类型" width="120">
          <template #default="scope">
            <el-tag :type="getErrorTypeTagType(scope.row.errorType)">
              {{ scope.row.errorType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="knowledgePoints" label="知识点" />
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

    <el-card class="profile-card" v-if="errorProfile.length > 0">
      <template #header>
        <div class="card-header">
          <span>错误档案</span>
        </div>
      </template>

      <el-table :data="errorProfile" style="width: 100%">
        <el-table-column prop="knowledgePoint" label="知识点" />
        <el-table-column prop="errorType" label="错误类型" width="120" />
        <el-table-column prop="errorCount" label="错误次数" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.errorCount >= 2 ? 'danger' : 'warning'">
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
    '无错误': 'correct'
  }
  return map[type] || 'unknown'
}

const getErrorTypeTagType = (type) => {
  const map = {
    '概念误解型': 'danger',
    '计算疏忽型': 'warning',
    '迁移困难型': 'primary',
    '逻辑断层型': 'info',
    '无错误': 'success'
  }
  return map[type] || 'info'
}

const handleQuestionUpload = (response) => {
  form.value.questionText = response.text
  ElMessage.success('题目识别成功')
}

const handleAnswerUpload = (response) => {
  form.value.userAnswer = response.text
  ElMessage.success('解答识别成功')
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
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

.upload-card, .result-card {
  margin-bottom: 20px;
}

.diagnosis-content {
  padding: 10px;
}

.error-type-tag {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.error-type-tag.concept {
  background: #fef0f0;
  color: #f56c6c;
}

.error-type-tag.calculation {
  background: #fdf6ec;
  color: #e6a23c;
}

.error-type-tag.transfer {
  background: #ecf5ff;
  color: #409eff;
}

.error-type-tag.logic {
  background: #f4f4f5;
  color: #909399;
}

.error-type-tag.correct {
  background: #f0f9eb;
  color: #67c23a;
}

.section {
  margin-bottom: 20px;
}

.section h4 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section p {
  color: #606266;
  line-height: 1.8;
}

.hint-section {
  background: #ecf5ff;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.example-box {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  font-family: monospace;
  white-space: pre-wrap;
}

.solution-content {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  white-space: pre-wrap;
  line-height: 1.8;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.history-card, .profile-card {
  margin-top: 20px;
}
</style>

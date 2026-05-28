<template>
  <div class="exam-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Edit /></el-icon>
        智能练习
      </h2>
      <p class="page-desc">基于你的错误档案，生成个性化练习题</p>
    </div>

    <el-row :gutter="24">
      <el-col :span="17">
        <el-card class="exam-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon><Document /></el-icon>
                <span>个性化练习题</span>
              </div>
              <div>
                <el-button type="primary" text @click="generateExam" :loading="loading" class="refresh-btn">
                  <el-icon v-if="!loading"><Refresh /></el-icon>
                  生成新题目
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="loading" class="loading-state">
            <el-skeleton :rows="8" animated />
            <div class="loading-text">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span>正在为你生成个性化练习题，请稍候...</span>
            </div>
          </div>

          <div v-else-if="currentExam" class="exam-content">
            <div class="exam-text" v-html="renderMarkdown(currentExam)"></div>

            <el-divider class="exam-divider" />

            <div class="exam-actions">
              <el-button type="primary" @click="goToDiagnose" size="large" class="diagnose-btn">
                <el-icon><Search /></el-icon>
                完成练习，去诊断
              </el-button>
            </div>
          </div>

          <div v-else class="empty-state">
            <el-empty description="点击上方按钮生成个性化练习题" :image-size="100">
              <template #image>
                <div class="empty-icon">
                  <el-icon :size="64"><Edit /></el-icon>
                </div>
              </template>
              <el-button type="primary" @click="generateExam" :loading="loading" size="large">生成练习</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :span="7">
        <el-card class="profile-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>学习档案</span>
            </div>
          </template>

          <div v-if="profile">
            <div class="stats-container">
              <div class="stat-item">
                <div class="stat-label">薄弱知识点数</div>
                <div class="stat-value">{{ profile.totalErrors }}</div>
              </div>

              <div class="stat-item">
                <div class="stat-label">总错误次数</div>
                <div class="stat-value error">{{ profile.totalErrorCount }}</div>
              </div>
            </div>

            <el-divider class="profile-divider" />

            <div v-if="profile.needAttention && profile.needAttention.length > 0">
              <h4 class="section-title">
                <el-icon><Warning /></el-icon>
                需要重点关注
              </h4>
              <div
                v-for="item in profile.needAttention"
                :key="item.id"
                class="attention-item"
                @click="generateSpecificExam(item.knowledgePoint, item.errorType)"
              >
                <div class="attention-info">
                  <div class="knowledge-point">{{ item.knowledgePoint }}</div>
                  <el-tag size="small" :type="getErrorTypeTagType(item.errorType)" class="error-type-tag">
                    {{ item.errorType }}
                  </el-tag>
                  <div v-if="item.errorDetails" class="error-details">
                    {{ truncate(item.errorDetails, 50) }}
                  </div>
                </div>
                <el-tag type="danger" effect="light" class="count-tag">
                  {{ item.errorCount }}次
                </el-tag>
              </div>
            </div>

            <div v-else class="no-attention">
              <el-empty description="暂无需要重点关注的知识点" :image-size="80" />
            </div>
          </div>

          <div v-else class="loading-profile">
            <el-skeleton :rows="5" animated />
          </div>
        </el-card>

        <el-card class="recommend-card" v-if="shouldRecommend" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>学习建议</span>
            </div>
          </template>
          
          <el-alert
            title="发现薄弱点"
            type="warning"
            :closable="false"
            show-icon
            class="recommend-alert"
          >
            <p>你在某些知识点上连续出现错误，建议进行针对性练习。</p>
            <el-button type="primary" @click="generateExam" class="recommend-btn">
              开始练习
            </el-button>
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { examApi } from '../api'
import { 
  Edit, 
  Loading, 
  Refresh, 
  Search, 
  Document, 
  User, 
  Warning,
  InfoFilled 
} from '@element-plus/icons-vue'

const router = useRouter()
const currentExam = ref('')
const profile = ref(null)
const shouldRecommend = ref(false)
const loading = ref(false)

const userId = 'user001'

const renderMarkdown = (content) => {
  return marked(content)
}

const getErrorTypeTagType = (type) => {
  const map = {
    '概念误解型': 'danger',
    '计算疏忽型': 'warning',
    '迁移困难型': 'primary',
    '逻辑断层型': 'info'
  }
  return map[type] || 'info'
}

const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const generateExam = async () => {
  loading.value = true
  try {
    const response = await examApi.generate(userId)
    currentExam.value = response.data.exam
    ElMessage.success('练习题生成成功')
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const generateSpecificExam = async (knowledgePoint, errorType) => {
  loading.value = true
  try {
    const response = await examApi.generateSpecific(knowledgePoint, errorType)
    currentExam.value = response.data.exam
    ElMessage.success('针对性练习生成成功')
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadProfile = async () => {
  try {
    const [profileRes, recommendRes] = await Promise.all([
      examApi.getProfile(userId),
      examApi.shouldRecommend(userId)
    ])
    profile.value = profileRes.data
    shouldRecommend.value = recommendRes.data.shouldRecommend
  } catch (error) {
    console.error('加载档案失败', error)
  }
}

const goToDiagnose = () => {
  router.push('/diagnose')
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.exam-page {
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
  color: #f687b3;
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

.exam-card :deep(.el-card__header),
.profile-card :deep(.el-card__header),
.recommend-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-bottom: 1px solid #e2e8f0;
}

.exam-card {
  margin-bottom: 24px;
  min-height: 500px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.exam-content {
  padding: 20px;
}

.exam-text {
  line-height: 1.9;
  font-size: 15px;
}

.exam-text :deep(h1), .exam-text :deep(h2), .exam-text :deep(h3) {
  margin: 24px 0 16px;
  color: #1a202c;
  font-weight: 700;
}

.exam-text :deep(h1) {
  font-size: 24px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 12px;
}

.exam-text :deep(h2) {
  font-size: 20px;
}

.exam-text :deep(h3) {
  font-size: 17px;
  color: #4a5568;
}

.exam-text :deep(p) {
  margin: 14px 0;
  color: #4a5568;
}

.exam-text :deep(ul), .exam-text :deep(ol) {
  margin: 14px 0;
  padding-left: 28px;
}

.exam-text :deep(li) {
  margin: 10px 0;
}

.exam-text :deep(code) {
  background: rgba(102, 126, 234, 0.1);
  padding: 3px 8px;
  border-radius: 6px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  color: #667eea;
}

.exam-divider {
  margin: 32px 0;
  border-color: #e2e8f0;
}

.exam-actions {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.diagnose-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 36px;
  height: 48px;
  font-size: 16px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-icon {
  color: #cbd5e0;
  margin-bottom: 16px;
}

.loading-state {
  padding: 40px 24px;
  min-height: 400px;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30px;
  color: #667eea;
  font-size: 16px;
  gap: 10px;
}

.loading-icon {
  animation: rotating 2s linear infinite;
  font-size: 20px;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.profile-card, .recommend-card {
  margin-bottom: 24px;
}

.stats-container {
  padding: 8px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid #e2e8f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #4a5568;
  font-size: 15px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #667eea;
}

.stat-value.error {
  color: #f56565;
}

.profile-divider {
  margin: 20px 0;
  border-color: #e2e8f0;
}

.section-title {
  font-size: 16px;
  color: #1a202c;
  margin: 0 0 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
}

.section-title :deep(.el-icon) {
  color: #ed8936;
}

.attention-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 18px;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  margin-bottom: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.attention-item:hover {
  background: #fff;
  border-color: #cbd5e0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.attention-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.knowledge-point {
  font-weight: 700;
  color: #1a202c;
  font-size: 15px;
}

.error-type-tag {
  font-weight: 600;
}

.error-details {
  font-size: 13px;
  color: #718096;
  line-height: 1.5;
}

.count-tag {
  font-weight: 700;
  flex-shrink: 0;
}

.no-attention {
  padding: 28px 0;
}

.loading-profile {
  padding: 28px 0;
}

.recommend-alert :deep(.el-alert__title) {
  font-weight: 700;
}

.recommend-btn {
  margin-top: 16px;
  width: 100%;
}
</style>

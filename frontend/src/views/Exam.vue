<template>
  <div class="exam-page">
    <h2 class="page-title">
      <el-icon><Edit /></el-icon>
      出题教练
    </h2>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="exam-card">
          <template #header>
            <div class="card-header">
              <span>个性化练习题</span>
              <div>
                <el-button type="primary" text @click="generateExam">
                  <el-icon><Refresh /></el-icon>
                  生成新题目
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="currentExam" class="exam-content">
            <div class="exam-text" v-html="renderMarkdown(currentExam)"></div>
            
            <el-divider />
            
            <div class="exam-actions">
              <el-button type="primary" @click="goToDiagnose">
                <el-icon><Search /></el-icon>
                完成练习，去诊断
              </el-button>
            </div>
          </div>

          <div v-else class="empty-state">
            <el-empty description="点击上方按钮生成个性化练习题">
              <el-button type="primary" @click="generateExam">生成练习</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>学习档案</span>
            </div>
          </template>

          <div v-if="profile">
            <div class="stat-item">
              <div class="stat-label">薄弱知识点数</div>
              <div class="stat-value">{{ profile.totalErrors }}</div>
            </div>

            <div class="stat-item">
              <div class="stat-label">总错误次数</div>
              <div class="stat-value">{{ profile.totalErrorCount }}</div>
            </div>

            <el-divider />

            <div v-if="profile.needAttention && profile.needAttention.length > 0">
              <h4 class="section-title">需要重点关注</h4>
              <div 
                v-for="item in profile.needAttention" 
                :key="item.id"
                class="attention-item"
                @click="generateSpecificExam(item.knowledgePoint, item.errorType)"
              >
                <div class="attention-info">
                  <div class="knowledge-point">{{ item.knowledgePoint }}</div>
                  <el-tag size="small" :type="getErrorTypeTagType(item.errorType)">
                    {{ item.errorType }}
                  </el-tag>
                </div>
                <el-tag type="danger" effect="dark">
                  {{ item.errorCount }}次
                </el-tag>
              </div>
            </div>

            <div v-else class="no-attention">
              <el-empty description="暂无需要重点关注的知识点" />
            </div>
          </div>

          <div v-else class="loading-profile">
            <el-skeleton :rows="5" animated />
          </div>
        </el-card>

        <el-card class="recommend-card" v-if="shouldRecommend">
          <template #header>
            <div class="card-header">
              <span>学习建议</span>
            </div>
          </template>
          
          <el-alert
            title="发现薄弱点"
            type="warning"
            :closable="false"
            show-icon
          >
            <p>你在某些知识点上连续出现错误，建议进行针对性练习。</p>
            <el-button type="primary" size="small" @click="generateExam" style="margin-top: 10px;">
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

.exam-card {
  margin-bottom: 20px;
  min-height: 500px;
}

.exam-content {
  padding: 10px;
}

.exam-text {
  line-height: 1.8;
}

.exam-text :deep(h1), .exam-text :deep(h2), .exam-text :deep(h3) {
  margin: 20px 0 12px;
  color: #303133;
}

.exam-text :deep(p) {
  margin: 12px 0;
  color: #606266;
}

.exam-text :deep(ul), .exam-text :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.exam-text :deep(li) {
  margin: 8px 0;
}

.exam-actions {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.profile-card, .recommend-card {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e4e7ed;
}

.stat-label {
  color: #606266;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.section-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.attention-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: background 0.3s;
}

.attention-item:hover {
  background: #ecf5ff;
}

.attention-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.knowledge-point {
  font-weight: 500;
  color: #303133;
}

.no-attention {
  padding: 20px 0;
}

.loading-profile {
  padding: 20px 0;
}
</style>

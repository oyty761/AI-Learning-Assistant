<template>
  <div class="analysis-page">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="$router.push('/')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
        <h2 class="page-title">
          <el-icon><TrendCharts /></el-icon>
          学习分析
        </h2>
      </div>
      <p class="page-desc">查看学习数据，了解进步轨迹</p>
    </div>

    <!-- 模块一：数据看板 -->
    <el-card class="dashboard-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><DataLine /></el-icon>
          <span>学习数据看板</span>
        </div>
      </template>
      
      <el-row :gutter="16" class="stats-row">
        <el-col :span="4" v-for="stat in dashboardStats" :key="stat.key">
          <div class="stat-card" :style="{ backgroundColor: stat.bgColor }">
            <div class="stat-icon" :style="{ color: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 模块二：学习活跃度日历 -->
    <el-card class="calendar-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Calendar /></el-icon>
          <span>学习活跃度</span>
        </div>
      </template>
      
      <div class="calendar-container">
        <div class="calendar-legend">
          <span class="legend-label">少</span>
          <div class="legend-item" style="background-color: #ebedf0;"></div>
          <div class="legend-item" style="background-color: #9be9a8;"></div>
          <div class="legend-item" style="background-color: #40c463;"></div>
          <div class="legend-item" style="background-color: #30a14e;"></div>
          <div class="legend-item" style="background-color: #216e39;"></div>
          <span class="legend-label">多</span>
        </div>
        
        <div class="calendar-grid">
          <div v-for="(week, weekIndex) in calendarWeeks" :key="weekIndex" class="calendar-week">
            <div
              v-for="day in week"
              :key="day.date"
              class="calendar-day"
              :style="{ backgroundColor: getActivityColor(day.count) }"
              :title="`${day.date}: ${day.count || 0} 次活动`"
            ></div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 模块三：智能学习报告 -->
    <el-card class="report-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Document /></el-icon>
            <span>智能学习报告</span>
          </div>
          <el-button type="primary" text @click="loadReport" :loading="reportLoading">
            <el-icon><Refresh /></el-icon>
            刷新报告
          </el-button>
        </div>
      </template>
      
      <div v-if="reportLoading" class="report-loading">
        <el-skeleton :rows="6" animated />
      </div>
      
      <div v-else-if="report" class="report-content">
        <!-- 薄弱点Top5 -->
        <div v-if="report.weakPoints && report.weakPoints.length > 0" class="weak-points-section">
          <h4 class="section-title">
            <el-icon><Warning /></el-icon>
            薄弱知识点 Top{{ Math.min(report.weakPoints.length, 5) }}
          </h4>
          <div class="weak-points-list">
            <div
              v-for="(point, index) in report.weakPoints"
              :key="index"
              class="weak-point-item"
            >
              <div class="point-rank">{{ index + 1 }}</div>
              <div class="point-info">
                <div class="point-name">{{ point.knowledgePoint }}</div>
                <el-tag size="small" :type="getErrorTypeTagType(point.errorType)" class="error-type-tag">
                  {{ point.errorType }}
                </el-tag>
              </div>
              <div class="point-count">
                <el-tag type="danger" effect="light">{{ point.errorCount }}次</el-tag>
              </div>
            </div>
          </div>
        </div>
        
        <el-divider v-if="report.weakPoints && report.weakPoints.length > 0" />
        
        <!-- AI报告内容 -->
        <div v-if="report.aiReport" class="ai-report-section">
          <div class="ai-report-content" v-html="renderMarkdown(report.aiReport)"></div>
        </div>
        
        <div v-else class="empty-report">
          <el-empty description="暂无学习报告数据" :image-size="80" />
        </div>
      </div>
      
      <div v-else class="empty-report">
        <el-empty description="点击刷新按钮生成学习报告" :image-size="80">
          <el-button type="primary" @click="loadReport" :loading="reportLoading">
            生成报告
          </el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { analysisApi } from '../api'
import {
  ArrowLeft,
  TrendCharts,
  DataLine,
  Calendar,
  Document,
  Refresh,
  Warning,
  Search,
  DocumentCopy,
  ChatDotRound,
  List,
  Check
} from '@element-plus/icons-vue'

const userId = 'user001'
const loading = ref(false)
const reportLoading = ref(false)

// 看板统计数据
const stats = ref({
  totalDiagnose: 0,
  weeklyActivity: 0,
  weakPoints: 0,
  todoCompletionRate: 0,
  todoCompleted: 0,
  todoTotal: 0,
  noteCount: 0,
  qaCount: 0
})

// 日历数据
const activityData = ref([])

// 报告数据
const report = ref(null)

// 看板统计配置
const dashboardStats = computed(() => [
  {
    key: 'totalDiagnose',
    label: '总诊断次数',
    value: stats.value.totalDiagnose,
    icon: 'Search',
    color: '#e53e3e',
    bgColor: 'rgba(245, 101, 101, 0.1)'
  },
  {
    key: 'weeklyActivity',
    label: '本周活跃度',
    value: stats.value.weeklyActivity,
    icon: 'DataLine',
    color: '#667eea',
    bgColor: 'rgba(102, 126, 234, 0.1)'
  },
  {
    key: 'weakPoints',
    label: '薄弱知识点',
    value: stats.value.weakPoints,
    icon: 'Warning',
    color: '#ed8936',
    bgColor: 'rgba(237, 137, 54, 0.1)'
  },
  {
    key: 'todoCompletion',
    label: '任务完成率',
    value: `${stats.value.todoCompletionRate}%`,
    icon: 'Check',
    color: '#48bb78',
    bgColor: 'rgba(72, 187, 120, 0.1)'
  },
  {
    key: 'noteCount',
    label: '笔记数量',
    value: stats.value.noteCount,
    icon: 'DocumentCopy',
    color: '#9f7aea',
    bgColor: 'rgba(159, 122, 234, 0.1)'
  },
  {
    key: 'qaCount',
    label: '问答次数',
    value: stats.value.qaCount,
    icon: 'ChatDotRound',
    color: '#38b2ac',
    bgColor: 'rgba(56, 178, 172, 0.1)'
  }
])

// 日历周数据
const calendarWeeks = computed(() => {
  const weeks = []
  const days = generateCalendarDays()
  
  for (let i = 0; i < days.length; i += 7) {
    weeks.push(days.slice(i, i + 7))
  }
  
  return weeks
})

// 生成日历天数
const generateCalendarDays = () => {
  const days = []
  const endDate = new Date()
  const startDate = new Date()
  startDate.setMonth(startDate.getMonth() - 6)
  
  for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
    const dateStr = d.toISOString().split('T')[0]
    const activity = activityData.value.find(a => a.date === dateStr)
    days.push({
      date: dateStr,
      count: activity ? activity.count : 0
    })
  }
  
  return days
}

// 获取活跃度颜色
const getActivityColor = (count) => {
  if (count === 0) return '#ebedf0'
  if (count === 1) return '#9be9a8'
  if (count === 2) return '#40c463'
  if (count === 3) return '#30a14e'
  return '#216e39'
}

// 获取错误类型标签样式
const getErrorTypeTagType = (type) => {
  const map = {
    '概念误解型': 'danger',
    '计算疏忽型': 'warning',
    '迁移困难型': 'primary',
    '逻辑断层型': 'info',
    '解答正确': 'success'
  }
  return map[type] || 'info'
}

// 渲染Markdown
const renderMarkdown = (content) => {
  return marked(content)
}

// 加载看板数据
const loadDashboard = async () => {
  try {
    const response = await analysisApi.getDashboard(userId)
    stats.value = response.data
  } catch (error) {
    console.error('加载看板数据失败', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载日历数据
const loadCalendar = async () => {
  try {
    const response = await analysisApi.getActivityCalendar(userId)
    activityData.value = response.data
  } catch (error) {
    console.error('加载日历数据失败', error)
  }
}

// 加载报告
const loadReport = async () => {
  reportLoading.value = true
  try {
    const response = await analysisApi.getReport(userId)
    report.value = response.data
  } catch (error) {
    console.error('加载报告失败', error)
    ElMessage.error('生成报告失败')
  } finally {
    reportLoading.value = false
  }
}

onMounted(() => {
  loadDashboard()
  loadCalendar()
  loadReport()
})
</script>

<style scoped>
.analysis-page {
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.back-btn {
  padding: 8px 12px;
  font-size: 14px;
  color: #666;
}

.back-btn:hover {
  color: #667eea;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 24px;
  color: #1a202c;
  font-weight: 600;
}

.page-title .el-icon {
  color: #667eea;
}

.page-desc {
  color: #718096;
  font-size: 14px;
  margin: 0 0 0 88px;
}

/* 卡片样式 */
.dashboard-card,
.calendar-card,
.report-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1a202c;
  font-size: 16px;
}

.card-header .header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 0;
}

/* 看板统计 */
.stats-row {
  margin: 0;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 12px;
  border-radius: 12px;
  text-align: center;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

/* 日历样式 */
.calendar-container {
  padding: 16px;
}

.calendar-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  margin-bottom: 12px;
}

.legend-label {
  font-size: 12px;
  color: #666;
  margin: 0 4px;
}

.legend-item {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.calendar-grid {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.calendar-week {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.calendar-day {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  cursor: pointer;
  transition: transform 0.2s;
}

.calendar-day:hover {
  transform: scale(1.2);
}

/* 报告样式 */
.report-loading {
  padding: 20px;
}

.report-content {
  padding: 8px;
}

.weak-points-section {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #1a202c;
  margin: 0 0 16px;
  font-weight: 600;
}

.section-title .el-icon {
  color: #ed8936;
}

.weak-points-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.weak-point-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 10px;
}

.point-rank {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-weight: 600;
  font-size: 14px;
}

.point-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.point-name {
  font-weight: 600;
  color: #1a202c;
  font-size: 14px;
}

.error-type-tag {
  font-weight: 500;
}

.point-count {
  font-weight: 600;
}

.ai-report-section {
  padding: 8px 0;
}

.ai-report-content {
  line-height: 1.8;
  color: #4a5568;
}

.ai-report-content :deep(h2) {
  font-size: 18px;
  color: #1a202c;
  margin: 20px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

.ai-report-content :deep(ul) {
  padding-left: 20px;
  margin: 12px 0;
}

.ai-report-content :deep(li) {
  margin: 8px 0;
}

.ai-report-content :deep(strong) {
  color: #1a202c;
}

.empty-report {
  padding: 40px 0;
}
</style>

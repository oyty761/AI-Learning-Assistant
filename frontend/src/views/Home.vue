<template>
  <div class="home">
    <div class="hero">
      <div class="hero-content">
        <h1 class="hero-title">AI数学思维学习助手</h1>
        <p class="hero-subtitle">识别个人学习瓶颈 · 动态介入 · 闭环反馈</p>
      </div>
    </div>

    <div class="modules">
      <el-row :gutter="20">
        <el-col :span="12" v-for="module in modules" :key="module.name">
          <el-card class="module-card" shadow="hover" @click="$router.push(module.path)">
            <div class="module-content">
              <div class="module-icon-wrapper" :style="{ backgroundColor: module.bgColor }">
                <el-icon :size="40" :color="module.color">
                  <component :is="module.icon" />
                </el-icon>
              </div>
              <div class="module-info">
                <h3>{{ module.title }}</h3>
                <p>{{ module.description }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <!-- 待办事项卡片 -->
        <el-col :span="12">
          <TodoCard />
        </el-col>
      </el-row>
    </div>

    <div class="features">
      <div class="features-header">
        <h2 class="section-title">核心功能</h2>
        <p class="section-subtitle">基于AI技术的智能学习闭环系统</p>
      </div>
      <el-row :gutter="24">
        <el-col :span="8" v-for="feature in features" :key="feature.title">
          <div class="feature-item">
            <div class="feature-icon-wrapper" :style="{ backgroundColor: feature.bgColor }">
              <el-icon :size="32" :color="feature.color"><component :is="feature.icon" /></el-icon>
            </div>
            <h4>{{ feature.title }}</h4>
            <p>{{ feature.desc }}</p>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import TodoCard from '../components/TodoCard.vue'

// 活力淡色调配色方案 - 加入暖色调平衡
const modules = [
  {
    name: 'notes',
    title: '智能笔记',
    description: '上传PPT/PDF，AI生成结构化学习笔记',
    icon: 'Document',
    color: '#48bb78',
    bgColor: 'rgba(72, 187, 120, 0.12)',
    path: '/notes'
  },
  {
    name: 'tutor',
    title: 'AI问答',
    description: '随时提问，AI用通俗语言解答疑惑',
    icon: 'ChatDotRound',
    color: '#4299e1',
    bgColor: 'rgba(66, 153, 225, 0.12)',
    path: '/tutor'
  },
  {
    name: 'diagnose',
    title: '错题诊断',
    description: '上传错题，AI分析错误原因并给出建议',
    icon: 'Search',
    color: '#ed8936',
    bgColor: 'rgba(237, 137, 54, 0.12)',
    path: '/diagnose'
  },
  {
    name: 'exam',
    title: '智能练习',
    description: '根据薄弱点，AI生成针对性练习题',
    icon: 'Edit',
    color: '#f687b3',
    bgColor: 'rgba(246, 135, 179, 0.12)',
    path: '/exam'
  }
]

const features = [
  {
    title: '学习闭环',
    desc: '从诊断到练习的完整学习闭环',
    icon: 'Refresh',
    color: '#38b2ac',
    bgColor: 'rgba(56, 178, 172, 0.12)'
  },
  {
    title: '个性化推荐',
    desc: '基于错误档案的智能推荐',
    icon: 'User',
    color: '#f6d365',
    bgColor: 'rgba(246, 211, 101, 0.12)'
  },
  {
    title: 'AI驱动',
    desc: '大模型赋能的智能学习助手',
    icon: 'Cpu',
    color: '#9f7aea',
    bgColor: 'rgba(159, 122, 234, 0.12)'
  }
]
</script>

<style scoped>
.home {
  padding: 0;
  max-width: 1400px;
  margin: 0 auto;
}

/* Hero区域 - 小清新渐变 */
.hero {
  text-align: center;
  padding: 60px 40px;
  background: linear-gradient(135deg, #a8d8ea 0%, #d4a5d9 50%, #f7dc6f 100%);
  border-radius: 20px;
  margin-bottom: 40px;
  color: #fff;
  box-shadow: 0 8px 32px rgba(168, 216, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.3;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
  letter-spacing: 1px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.95;
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* 模块卡片区域 - 优化间距 */
.modules {
  margin-bottom: 40px;
}

.module-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px;
  border: none;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.module-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.module-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px;
}

.module-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.module-card:hover .module-icon-wrapper {
  transform: scale(1.08);
}

.module-info h3 {
  font-size: 22px;
  margin-bottom: 8px;
  color: #1a202c;
  font-weight: 700;
}

.module-info p {
  color: #718096;
  font-size: 15px;
  line-height: 1.6;
}

/* 核心功能区域 - 优化布局 */
.features {
  background: linear-gradient(180deg, #f7fafc 0%, #edf2f7 100%);
  padding: 48px 40px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
}

.features-header {
  text-align: center;
  margin-bottom: 36px;
}

.section-title {
  font-size: 32px;
  margin-bottom: 12px;
  color: #1a202c;
  font-weight: 700;
}

.section-subtitle {
  font-size: 16px;
  color: #718096;
  font-weight: 400;
}

.feature-item {
  text-align: center;
  padding: 32px 24px;
  background: #fff;
  border-radius: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}

.feature-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.1);
  border-color: #cbd5e0;
}

.feature-icon-wrapper {
  width: 68px;
  height: 68px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  transition: all 0.3s ease;
}

.feature-item:hover .feature-icon-wrapper {
  transform: scale(1.1);
}

.feature-item h4 {
  font-size: 20px;
  margin: 0 0 12px;
  color: #1a202c;
  font-weight: 700;
}

.feature-item p {
  color: #718096;
  font-size: 15px;
  line-height: 1.6;
  margin: 0;
}
</style>

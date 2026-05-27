<template>
  <div class="home">
    <div class="hero">
      <div class="hero-content">
        <h1 class="hero-title">AI数学思维学习助手</h1>
        <p class="hero-subtitle">识别个人学习瓶颈 · 动态介入 · 闭环反馈</p>
      </div>
    </div>

    <div class="modules">
      <el-row :gutter="24">
        <el-col :span="12" v-for="module in modules" :key="module.name">
          <el-card class="module-card" shadow="hover" @click="$router.push(module.path)">
            <div class="module-content">
              <div class="module-icon-wrapper" :style="{ backgroundColor: module.bgColor }">
                <el-icon :size="36" :color="module.color">
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
      </el-row>
    </div>

    <div class="features">
      <div class="features-header">
        <h2 class="section-title">核心功能</h2>
        <p class="section-subtitle">基于AI技术的智能学习闭环系统</p>
      </div>
      <el-row :gutter="32">
        <el-col :span="8" v-for="feature in features" :key="feature.title">
          <div class="feature-item">
            <div class="feature-icon-wrapper" :style="{ backgroundColor: feature.bgColor }">
              <el-icon :size="28" :color="feature.color"><component :is="feature.icon" /></el-icon>
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
// 莫兰迪色系配色方案
const modules = [
  {
    name: 'notes',
    title: '智能笔记',
    description: '上传PPT/PDF，AI生成结构化学习笔记',
    icon: 'Document',
    color: '#7a9e7e',
    bgColor: 'rgba(122, 158, 126, 0.15)',
    path: '/notes'
  },
  {
    name: 'tutor',
    title: 'AI问答',
    description: '随时提问，AI用通俗语言解答疑惑',
    icon: 'ChatDotRound',
    color: '#6b8cae',
    bgColor: 'rgba(107, 140, 174, 0.15)',
    path: '/tutor'
  },
  {
    name: 'diagnose',
    title: '错题诊断',
    description: '上传错题，AI分析错误原因并给出建议',
    icon: 'Search',
    color: '#c4a77d',
    bgColor: 'rgba(196, 167, 125, 0.15)',
    path: '/diagnose'
  },
  {
    name: 'exam',
    title: '智能练习',
    description: '根据薄弱点，AI生成针对性练习题',
    icon: 'Edit',
    color: '#b88a8a',
    bgColor: 'rgba(184, 138, 138, 0.15)',
    path: '/exam'
  }
]

const features = [
  {
    title: '学习闭环',
    desc: '从诊断到练习的完整学习闭环',
    icon: 'Refresh',
    color: '#8fa3b8',
    bgColor: 'rgba(143, 163, 184, 0.15)'
  },
  {
    title: '个性化推荐',
    desc: '基于错误档案的智能推荐',
    icon: 'User',
    color: '#a5b5a5',
    bgColor: 'rgba(165, 181, 165, 0.15)'
  },
  {
    title: 'AI驱动',
    desc: '大模型赋能的智能学习助手',
    icon: 'Cpu',
    color: '#b8a5a5',
    bgColor: 'rgba(184, 165, 165, 0.15)'
  }
]
</script>

<style scoped>
.home {
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero区域 - 使用柔和的莫兰迪渐变 */
.hero {
  text-align: center;
  padding: 80px 40px;
  background: linear-gradient(135deg, #9db4c4 0%, #a8b5a0 50%, #c4b5a0 100%);
  border-radius: 16px;
  margin-bottom: 48px;
  color: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 42px;
  font-weight: 600;
  margin-bottom: 20px;
  letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 18px;
  opacity: 0.95;
  font-weight: 400;
  letter-spacing: 1px;
}

/* 模块卡片区域 */
.modules {
  margin-bottom: 56px;
}

.module-card {
  margin-bottom: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.module-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px;
}

.module-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.module-card:hover .module-icon-wrapper {
  transform: scale(1.05);
}

.module-info h3 {
  font-size: 22px;
  margin-bottom: 10px;
  color: #3a3a3a;
  font-weight: 600;
}

.module-info p {
  color: #666;
  font-size: 15px;
  line-height: 1.6;
}

/* 核心功能区域 */
.features {
  background: #faf9f7;
  padding: 56px 48px;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.04);
}

.features-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: 28px;
  margin-bottom: 12px;
  color: #3a3a3a;
  font-weight: 600;
}

.section-subtitle {
  font-size: 15px;
  color: #888;
  font-weight: 400;
}

.feature-item {
  text-align: center;
  padding: 32px 24px;
  background: #fff;
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.feature-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.feature-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  transition: transform 0.3s ease;
}

.feature-item:hover .feature-icon-wrapper {
  transform: scale(1.08);
}

.feature-item h4 {
  font-size: 18px;
  margin: 0 0 12px;
  color: #3a3a3a;
  font-weight: 600;
}

.feature-item p {
  color: #777;
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
}
</style>

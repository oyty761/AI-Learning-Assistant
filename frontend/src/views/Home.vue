<template>
  <div class="home">
    <div class="hero">
      <h1 class="hero-title">AI数学思维学习助手</h1>
      <p class="hero-subtitle">识别个人学习瓶颈 · 动态介入 · 闭环反馈</p>
    </div>

    <div class="modules">
      <el-row :gutter="20">
        <el-col :span="12" v-for="module in modules" :key="module.name">
          <el-card class="module-card" shadow="hover" @click="$router.push(module.path)">
            <div class="module-content">
              <el-icon :size="48" :color="module.color">
                <component :is="module.icon" />
              </el-icon>
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
      <h2 class="section-title">核心功能</h2>
      <el-row :gutter="20">
        <el-col :span="8" v-for="feature in features" :key="feature.title">
          <div class="feature-item">
            <el-icon :size="32" color="#409eff"><component :is="feature.icon" /></el-icon>
            <h4>{{ feature.title }}</h4>
            <p>{{ feature.desc }}</p>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
const modules = [
  {
    name: 'notes',
    title: '笔记整理官',
    description: '上传PPT/PDF，生成结构化学习笔记',
    icon: 'Document',
    color: '#67c23a',
    path: '/notes'
  },
  {
    name: 'tutor',
    title: '对话导师',
    description: '苏格拉底式问答，深入理解概念',
    icon: 'ChatDotRound',
    color: '#409eff',
    path: '/tutor'
  },
  {
    name: 'diagnose',
    title: '解题诊断师',
    description: '分析解题错误，针对性反馈',
    icon: 'Search',
    color: '#e6a23c',
    path: '/diagnose'
  },
  {
    name: 'exam',
    title: '出题教练',
    description: '个性化练习，巩固薄弱点',
    icon: 'Edit',
    color: '#f56c6c',
    path: '/exam'
  }
]

const features = [
  {
    title: '学习闭环',
    desc: '从诊断到练习的完整学习闭环',
    icon: 'Refresh'
  },
  {
    title: '个性化推荐',
    desc: '基于错误档案的智能推荐',
    icon: 'User'
  },
  {
    title: 'AI驱动',
    desc: '大模型赋能的智能学习助手',
    icon: 'Cpu'
  }
]
</script>

<style scoped>
.home {
  padding: 20px 0;
}

.hero {
  text-align: center;
  padding: 60px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 40px;
  color: white;
}

.hero-title {
  font-size: 36px;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 18px;
  opacity: 0.9;
}

.modules {
  margin-bottom: 40px;
}

.module-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.module-card:hover {
  transform: translateY(-5px);
}

.module-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.module-info h3 {
  font-size: 20px;
  margin-bottom: 8px;
  color: #303133;
}

.module-info p {
  color: #606266;
  font-size: 14px;
}

.features {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 30px;
  color: #303133;
}

.feature-item {
  text-align: center;
  padding: 20px;
}

.feature-item h4 {
  font-size: 18px;
  margin: 16px 0 8px;
  color: #303133;
}

.feature-item p {
  color: #606266;
  font-size: 14px;
}
</style>

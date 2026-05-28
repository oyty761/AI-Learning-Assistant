<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-header">
      <div class="logo">
        <el-icon size="32"><School /></el-icon>
        <span v-if="!isCollapsed" class="logo-text">AI学习助手</span>
      </div>
      <el-button
        v-if="isMobile"
        text
        class="collapse-btn"
        @click="$emit('close')"
      >
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: $route.path === item.path }"
      >
        <el-icon :size="24">
          <component :is="item.icon" />
        </el-icon>
        <span v-if="!isCollapsed" class="nav-text">{{ item.title }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="user-info" v-if="!isCollapsed">
        <el-avatar :size="36" :icon="UserFilled" />
        <span class="user-name">用户001</span>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  School,
  HomeFilled,
  Document,
  ChatDotRound,
  Search,
  Edit,
  CircleCheck,
  UserFilled,
  Close
} from '@element-plus/icons-vue'

const route = useRoute()
const isCollapsed = ref(false)
const isMobile = ref(false)

const menuItems = [
  { path: '/', title: '首页', icon: 'HomeFilled' },
  { path: '/notes', title: '智能笔记', icon: 'Document' },
  { path: '/tutor', title: 'AI问答', icon: 'ChatDotRound' },
  { path: '/diagnose', title: '错题诊断', icon: 'Search' },
  { path: '/exam', title: '智能练习', icon: 'Edit' },
  { path: '/todo', title: '待办事项', icon: 'CircleCheck' }
]

const checkScreenSize = () => {
  const width = window.innerWidth
  isMobile.value = width < 768
  isCollapsed.value = width < 1200 && width >= 768
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

defineEmits(['close'])
</script>

<style scoped>
.sidebar {
  width: 260px;
  height: 100vh;
  background: linear-gradient(180deg, #fff 0%, #f8fafc 100%);
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.05);
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #a8d8ea 0%, #d4a5d9 50%, #f7dc6f 100%);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}

.logo .el-icon {
  color: #ffd700;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  white-space: nowrap;
  color: #fff;
}

.collapse-btn {
  padding: 4px;
  color: rgba(255, 255, 255, 0.8);
}

.collapse-btn:hover {
  color: #fff;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  margin-bottom: 8px;
  border-radius: 12px;
  color: #4a5568;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(102, 126, 234, 0.08);
  color: #667eea;
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(118, 75, 162, 0.08) 100%);
  color: #667eea;
  font-weight: 600;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 28px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 3px 3px 0;
}

.nav-text {
  font-size: 15px;
  white-space: nowrap;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  background: #f7fafc;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
}

.user-name {
  font-size: 15px;
  color: #2d3748;
  font-weight: 600;
}

/* 移动端遮罩 */
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 2000;
  }
}
</style>

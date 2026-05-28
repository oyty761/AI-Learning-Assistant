<template>
  <div class="app">
    <!-- 首页使用传统顶部导航 -->
    <template v-if="isHomePage">
      <el-container>
        <el-header class="header">
          <div class="logo">
            <el-icon size="28"><School /></el-icon>
            <span>AI数学思维学习助手</span>
          </div>
          <el-menu
            :default-active="$route.path"
            class="nav-menu"
            mode="horizontal"
            router
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/notes">
              <el-icon><Document /></el-icon>
              <span>智能笔记</span>
            </el-menu-item>
            <el-menu-item index="/tutor">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI问答</span>
            </el-menu-item>
            <el-menu-item index="/diagnose">
              <el-icon><Search /></el-icon>
              <span>错题诊断</span>
            </el-menu-item>
            <el-menu-item index="/exam">
              <el-icon><Edit /></el-icon>
              <span>智能练习</span>
            </el-menu-item>
            <el-menu-item index="/todo">
              <el-icon><CircleCheck /></el-icon>
              <span>待办事项</span>
            </el-menu-item>
          </el-menu>
        </el-header>
        <el-main class="main-content home-content">
          <router-view />
        </el-main>
      </el-container>
    </template>

    <!-- 功能页面使用左侧导航 -->
    <template v-else>
      <div class="layout-with-sidebar">
        <Sidebar />
        <main class="main-content-with-sidebar">
          <router-view />
        </main>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './components/Sidebar.vue'

const $route = useRoute()
const isHomePage = computed(() => $route.path === '/')
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 16px;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f7fa;
  font-size: 16px;
  line-height: 1.6;
  color: #2c3e50;
}

.app {
  min-height: 100vh;
}

/* 顶部导航栏 - 小清新风格 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #a8d8ea 0%, #d4a5d9 50%, #f7dc6f 100%);
  box-shadow: 0 4px 20px rgba(168, 216, 234, 0.25);
  padding: 0 32px;
  flex-wrap: nowrap;
  border-bottom: none;
  height: 72px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  white-space: nowrap;
  flex-shrink: 0;
}

.logo .el-icon {
  color: #ffd700;
}

.nav-menu {
  border-bottom: none;
  flex: 1;
  display: flex;
  justify-content: flex-end;
  min-width: 0;
  background-color: transparent;
}

.nav-menu :deep(.el-menu-item) {
  padding: 0 20px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  border-bottom: none;
  height: 72px;
  line-height: 72px;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.15);
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #ffd700;
  font-weight: 600;
  border-bottom: 3px solid #ffd700;
  background-color: rgba(255, 255, 255, 0.1);
}

/* 主内容区域 - 优化留白 */
.main-content {
  padding: 24px;
  max-width: 100%;
  margin: 0 auto;
  width: 100%;
}

.home-content {
  max-width: 1400px;
  padding: 24px;
}

/* 左侧导航布局 */
.layout-with-sidebar {
  display: flex;
  min-height: 100vh;
}

.main-content-with-sidebar {
  flex: 1;
  margin-left: 260px;
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

/* 页面标题样式 - 清晰层级 */
.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 24px;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title .el-icon {
  color: #667eea;
}

.card-container {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
}

/* 全局卡片样式优化 - 现代风格 */
:deep(.el-card) {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 24px;
  font-weight: 600;
  color: #1a202c;
  font-size: 18px;
}

:deep(.el-card__body) {
  padding: 24px;
  font-size: 15px;
  color: #4a5568;
}

/* 按钮样式优化 - 活力配色 */
:deep(.el-button) {
  font-size: 15px;
  font-weight: 500;
  border-radius: 10px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  transform: translateY(-1px);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(72, 187, 120, 0.4);
}

:deep(.el-button--success:hover) {
  background: linear-gradient(135deg, #3da76a 0%, #2f8a58 100%);
  box-shadow: 0 6px 20px rgba(72, 187, 120, 0.5);
  transform: translateY(-1px);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(245, 101, 101, 0.4);
}

:deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #e04e4e 0%, #d32f2f 100%);
  box-shadow: 0 6px 20px rgba(245, 101, 101, 0.5);
  transform: translateY(-1px);
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(237, 137, 54, 0.4);
}

:deep(.el-button--info) {
  background: linear-gradient(135deg, #718096 0%, #4a5568 100%);
  border: none;
}

/* 输入框样式优化 - 清新风格 */
:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #cbd5e0 inset;
  font-size: 15px;
  transition: all 0.3s ease;
}

:deep(.el-input__inner) {
  font-size: 15px;
  color: #2d3748;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #a0aec0 inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.3), 0 0 0 1px #667eea inset;
}

/* 标签样式 - 活力配色 */
:deep(.el-tag) {
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  padding: 4px 10px;
}

:deep(.el-tag--primary) {
  background-color: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

:deep(.el-tag--success) {
  background-color: rgba(72, 187, 120, 0.1);
  border-color: rgba(72, 187, 120, 0.2);
  color: #48bb78;
}

:deep(.el-tag--warning) {
  background-color: rgba(237, 137, 54, 0.1);
  border-color: rgba(237, 137, 54, 0.2);
  color: #ed8936;
}

:deep(.el-tag--danger) {
  background-color: rgba(245, 101, 101, 0.1);
  border-color: rgba(245, 101, 101, 0.2);
  color: #f56565;
}

/* 菜单样式 - 增大字体 */
:deep(.el-menu-item) {
  font-size: 15px;
}

/* 表单标签 - 增大字体 */
:deep(.el-form-item__label) {
  font-size: 15px;
  font-weight: 500;
  color: #4a5568;
}

/* 表格样式 - 现代风格 */
:deep(.el-table) {
  font-size: 14px;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  font-size: 14px;
  font-weight: 600;
  background-color: #f7fafc;
  color: #2d3748;
}

:deep(.el-table td) {
  color: #4a5568;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #f7fafc;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
}

:deep(.el-dialog__body) {
  font-size: 15px;
  color: #4a5568;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 24px;
  margin-right: 0;
}

/* 下拉菜单样式 */
:deep(.el-select-dropdown__item) {
  font-size: 14px;
}

/* 日期选择器样式 */
:deep(.el-date-picker) {
  font-size: 14px;
}

/* 空状态样式优化 */
:deep(.el-empty__description) {
  font-size: 15px;
  color: #718096;
}

/* 分割线样式 */
:deep(.el-divider) {
  background-color: #e2e8f0;
}

/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

/* 空状态样式 */
:deep(.el-empty__description) {
  font-size: 17px;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .main-content-with-sidebar {
    margin-left: 80px;
  }
}

@media (max-width: 768px) {
  html {
    font-size: 16px;
  }

  .header {
    padding: 0 12px;
    height: 60px;
  }

  .logo {
    font-size: 20px;
  }

  .nav-menu :deep(.el-menu-item) {
    padding: 0 10px;
    font-size: 15px;
  }

  .main-content {
    padding: 12px 8px;
  }

  .main-content-with-sidebar {
    margin-left: 0;
    padding: 12px 8px;
  }

  .page-title {
    font-size: 28px;
  }
}
</style>

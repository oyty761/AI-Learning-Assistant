<template>
  <div class="app">
    <el-container>
      <el-header class="header">
        <div class="logo">
          <el-icon size="24"><School /></el-icon>
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
        </el-menu>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const $route = useRoute()
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f8f7f5;
}

.app {
  min-height: 100vh;
}

/* 顶部导航栏 - 使用莫兰迪色系 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0 32px;
  flex-wrap: nowrap;
  border-bottom: 1px solid #f0eeeb;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #7a8b99;
  white-space: nowrap;
  flex-shrink: 0;
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
  font-size: 15px;
  color: #666;
  transition: all 0.3s ease;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: #8fa3b8;
  background-color: transparent;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #6b8cae;
  font-weight: 500;
  border-bottom: 2px solid #6b8cae;
}

/* 主内容区域 - 增大使用面积 */
.main-content {
  padding: 24px 32px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

/* 页面标题样式 */
.page-title {
  font-size: 26px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #3a3a3a;
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-container {
  background: #fff;
  border-radius: 12px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 全局卡片样式优化 */
:deep(.el-card) {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0eeeb;
  padding: 18px 24px;
  font-weight: 500;
  color: #3a3a3a;
}

:deep(.el-card__body) {
  padding: 24px;
}

/* 按钮样式优化 */
:deep(.el-button--primary) {
  background-color: #8fa3b8;
  border-color: #8fa3b8;
}

:deep(.el-button--primary:hover) {
  background-color: #7a8fa3;
  border-color: #7a8fa3;
}

:deep(.el-button--success) {
  background-color: #7a9e7e;
  border-color: #7a9e7e;
}

:deep(.el-button--success:hover) {
  background-color: #6a8e6e;
  border-color: #6a8e6e;
}

/* 输入框样式优化 */
:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e0ddd8 inset;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c4b5a0 inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #8fa3b8 inset;
}
</style>

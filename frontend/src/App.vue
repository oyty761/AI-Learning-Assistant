<template>
  <div class="app">
    <el-container class="main-container">
      <!-- 左侧导航栏 - 所有页面统一显示 -->
      <el-aside class="sidebar" width="200px">
        <!-- Logo区域 -->
        <div class="sidebar-logo" @click="$router.push('/')">
          <el-icon size="22"><School /></el-icon>
          <span>AI学习助手</span>
        </div>
        
        <!-- 导航菜单 -->
        <el-menu
          :default-active="$route.path"
          class="sidebar-menu"
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
            <span>解题诊断</span>
          </el-menu-item>
          <el-menu-item index="/exam">
            <el-icon><Edit /></el-icon>
            <span>智能练习</span>
          </el-menu-item>
          <el-menu-item index="/analysis">
            <el-icon><TrendCharts /></el-icon>
            <span>学习分析</span>
          </el-menu-item>
          <el-menu-item index="/todo">
            <el-icon><List /></el-icon>
            <span>待办事项</span>
          </el-menu-item>
        </el-menu>
        
        <!-- 用户信息区域 -->
        <div class="sidebar-footer">
          <div class="user-info">
            <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
            <span class="user-name">用户001</span>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { UserFilled, TrendCharts } from '@element-plus/icons-vue'
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
  background-color: #f5f7fa;
}

.app {
  min-height: 100vh;
}

.main-container {
  min-height: 100vh;
}

/* 左侧导航栏 - 采用效果图中的渐变配色 */
.sidebar {
  background: linear-gradient(180deg, #e8d5e0 0%, #d4e5ed 50%, #c5dce8 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.08);
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}

/* Logo区域 */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

.sidebar-logo:hover {
  background: rgba(255, 255, 255, 0.15);
}

.sidebar-logo .el-icon {
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 8px;
  padding: 6px;
}

.sidebar-logo span {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 导航菜单 */
.sidebar-menu {
  border-right: none;
  background: transparent;
  flex: 1;
  padding: 12px 8px;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 4px 0;
  border-radius: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  padding: 0 12px !important;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.85);
  color: #7a8b99;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
  color: inherit;
}

/* 底部用户信息 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  background: linear-gradient(135deg, #9db4c4 0%, #a8b5a0 100%);
  color: #fff;
}

.user-name {
  font-size: 14px;
  color: #5a6a7a;
  font-weight: 500;
}

/* 主内容区域 */
.main-content {
  padding: 20px 24px;
  background: #f5f7fa;
  margin-left: 200px;
  min-height: 100vh;
  overflow-y: auto;
}

/* 页面标题样式 */
.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #3a3a3a;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 全局卡片样式优化 - Morandi色系 */
:deep(.el-card) {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0eeeb;
  padding: 16px 20px;
  font-weight: 500;
  color: #3a3a3a;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 按钮样式优化 - Morandi色系 */
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

/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(143, 163, 184, 0.4);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(143, 163, 184, 0.6);
}
</style>

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
            <span>笔记整理</span>
          </el-menu-item>
          <el-menu-item index="/tutor">
            <el-icon><ChatDotRound /></el-icon>
            <span>对话导师</span>
          </el-menu-item>
          <el-menu-item index="/diagnose">
            <el-icon><Search /></el-icon>
            <span>解题诊断</span>
          </el-menu-item>
          <el-menu-item index="/exam">
            <el-icon><Edit /></el-icon>
            <span>出题教练</span>
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
  background-color: #f5f7fa;
}

.app {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.nav-menu {
  border-bottom: none;
}

.main-content {
  padding: 20px 40px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

.card-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>

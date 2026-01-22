<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <img src="@/assets/logo.png" alt="Logo" />
        <span v-show="!collapsed">Auto API</span>
      </div>
      <Sidebar />
    </el-aside>
    <el-container class="layout-main">
      <el-header class="layout-header">
        <Header />
      </el-header>
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import { useAppStore } from '@/stores'

const appStore = useAppStore()
const collapsed = ref(appStore.sidebarCollapsed)
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  
  .layout-aside {
    background: #304156;
    transition: width 0.3s;
    overflow: hidden;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      background: #263445;
      color: #fff;
      font-weight: bold;
      font-size: 16px;
      
      img {
        width: 32px;
        height: 32px;
      }
    }
  }
  
  .layout-main {
    flex-direction: column;
    
    .layout-header {
      height: 60px;
      padding: 0;
      background: #fff;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    }
    
    .layout-content {
      padding: 20px;
      background: #f5f7fa;
      overflow-y: auto;
    }
  }
}
</style>

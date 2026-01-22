<template>
  <div id="app" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <template v-if="!$route.meta.hiddenLayout">
      <Sidebar :collapsed="sidebarCollapsed" @toggle="toggleSidebar" />
      <div class="main-container">
        <Header @toggle="toggleSidebar" />
        <div class="content-wrapper">
          <router-view />
        </div>
      </div>
    </template>
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script>
import Sidebar from '@/components/common/Sidebar.vue'
import Header from '@/components/common/Header.vue'

export default {
  name: 'App',
  components: {
    Sidebar,
    Header
  },
  data() {
    return {
      sidebarCollapsed: false
    }
  },
  methods: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    }
  }
}
</script>

<style lang="scss">
#app {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 220px;
  transition: margin-left 0.3s;
}

#app.sidebar-collapsed .main-container {
  margin-left: 64px;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}
</style>

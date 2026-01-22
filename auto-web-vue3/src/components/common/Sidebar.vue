<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="collapsed"
    :collapse-transition="false"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
    router
  >
    <template v-for="route in menuRoutes" :key="route.path">
      <el-menu-item
        v-if="!route.children || route.children.length === 1"
        :index="getMenuIndex(route)"
      >
        <el-icon><component :is="getIcon(route.path)" /></el-icon>
        <template #title>{{ route.meta?.title }}</template>
      </el-menu-item>
      
      <el-sub-menu
        v-else
        :index="route.path"
      >
        <template #title>
          <el-icon><component :is="getIcon(route.path)" /></el-icon>
          <span>{{ route.meta?.title }}</span>
        </template>
        <el-menu-item
          v-for="child in route.children"
          :key="child.path"
          :index="`${route.path}/${child.path}`"
        >
          <span>{{ child.meta?.title }}</span>
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const collapsed = computed(() => false)

const menuRoutes = computed(() => {
  const mainRoute = router.options.routes.find(r => r.path === '/')
  return mainRoute?.children?.filter(c => c.meta?.title) || []
})

function getMenuIndex(route) {
  if (route.children?.length === 1) {
    return `${route.path}/${route.children[0].path}`
  }
  return route.path
}

function getIcon(path) {
  const iconMap = {
    '/dashboard': 'Odometer',
    '/datasource': 'Connection',
    '/table-select': 'Grid',
    '/table-create': 'Plus',
    '/script-editor': 'Edit',
    '/api-docs': 'Document',
    '/api-test': 'DataLine'
  }
  return iconMap[path] || 'Menu'
}
</script>

<style lang="scss" scoped>
.el-menu {
  border-right: none;
}
</style>

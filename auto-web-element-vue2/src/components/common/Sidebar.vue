<template>
  <div class="sidebar" :class="{ collapsed }">
    <div class="logo-container">
      <img src="~@/assets/logo.png" alt="logo" class="logo" v-if="!collapsed" />
      <img src="~@/assets/logo-small.png" alt="logo" class="logo-small" v-else />
      <span class="title" v-if="!collapsed">Auto-API</span>
    </div>
    <el-menu
      :default-active="$route.path"
      :collapse="collapsed"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <template v-for="route in menuRoutes">
        <el-menu-item
          v-if="!route.children || route.meta.single"
          :key="route.path"
          :index="route.path"
        >
          <i :class="route.meta.icon"></i>
          <span slot="title">{{ route.meta.title }}</span>
        </el-menu-item>
        <el-submenu
          v-else
          :key="route.path"
          :index="route.path"
        >
          <template slot="title">
            <i :class="route.meta.icon"></i>
            <span slot="title">{{ route.meta.title }}</span>
          </template>
          <el-menu-item
            v-for="child in route.children"
            :key="child.path"
            :index="`${route.path}/${child.path}`"
          >
            <i :class="child.meta.icon"></i>
            <span slot="title">{{ child.meta.title }}</span>
          </el-menu-item>
        </el-submenu>
      </template>
    </el-menu>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Sidebar',
  props: {
    collapsed: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapGetters(['permission_routes']),
    menuRoutes() {
      return this.permission_routes.filter(route => !route.meta.hiddenMenu)
    }
  },
  created() {
    this.$store.dispatch('permission/generateRoutes').catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.sidebar {
  width: 220px;
  height: 100%;
  background-color: #304156;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;

  &.collapsed {
    width: 64px;
  }
}

.logo-container {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
  overflow: hidden;

  .logo {
    width: 32px;
    height: 32px;
    margin-right: 10px;
  }

  .logo-small {
    width: 28px;
    height: 28px;
  }

  .title {
    color: #fff;
    font-size: 16px;
    font-weight: bold;
    white-space: nowrap;
  }
}

.el-menu {
  flex: 1;
  border-right: none;

  &:not(.el-menu--collapse) {
    width: 220px;
  }

  .el-menu-item,
  .el-submenu__title {
    height: 50px;
    line-height: 50px;

    &:hover {
      background-color: #263445 !important;
    }
  }

  .el-menu-item.is-active {
    background-color: #1f2d3d !important;
  }
}
</style>

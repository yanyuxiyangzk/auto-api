<template>
  <header class="header">
    <div class="header-left">
      <el-button
        type="text"
        icon="el-icon-menu"
        @click="$emit('toggle')"
        class="toggle-btn"
      ></el-button>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
          <router-link v-if="item.path" :to="item.path">{{ item.title }}</router-link>
          <span v-else>{{ item.title }}</span>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-tooltip content="刷新" placement="bottom">
        <el-button type="text" icon="el-icon-refresh" @click="handleRefresh"></el-button>
      </el-tooltip>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="30" icon="el-icon-user"></el-avatar>
          <span class="username">管理员</span>
          <i class="el-icon-arrow-down"></i>
        </div>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="profile">
            <i class="el-icon-user"></i> 个人中心
          </el-dropdown-item>
          <el-dropdown-item command="settings">
            <i class="el-icon-setting"></i> 系统设置
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <i class="el-icon-switch-button"></i> 退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </header>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Header',
  computed: {
    ...mapGetters(['name']),
    breadcrumbs() {
      const matched = this.$route.matched.filter(item => item.meta && item.meta.title)
      return matched.map(item => ({
        title: item.meta.title,
        path: item.path !== this.$route.path ? item.path : ''
      }))
    }
  },
  methods: {
    handleRefresh() {
      this.$router.replace({
        path: '/redirect' + this.$route.fullPath
      })
    },
    handleCommand(command) {
      switch (command) {
        case 'logout':
          this.handleLogout()
          break
        case 'profile':
          this.$router.push('/profile')
          break
        case 'settings':
          this.$router.push('/settings')
          break
      }
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('user/logout')
        this.$router.push('/login')
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.header {
  height: 50px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;

  .toggle-btn {
    font-size: 20px;
    color: #606266;
    margin-right: 15px;
  }
}

.header-right {
  display: flex;
  align-items: center;

  .el-button {
    font-size: 18px;
    color: #606266;
    margin-left: 10px;
  }

  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 5px 10px;
    border-radius: 4px;

    &:hover {
      background-color: #f5f7fa;
    }

    .username {
      margin-left: 8px;
      color: #606266;
      font-size: 14px;
    }

    .el-icon-arrow-down {
      margin-left: 5px;
      color: #909399;
    }
  }
}
</style>

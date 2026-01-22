<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #409eff;">
            <i class="el-icon-connection"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.datasourceCount }}</div>
            <div class="stat-label">数据源</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #67c23a;">
            <i class="el-icon-menu"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.tableCount }}</div>
            <div class="stat-label">数据表</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #e6a23c;">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.apiCount }}</div>
            <div class="stat-label">API 接口</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: #f56c6c;">
            <i class="el-icon-edit-outline"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.scriptCount }}</div>
            <div class="stat-label">脚本</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="card">
          <div slot="header" class="card-header">
            <span>最近活动</span>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
              :timestamp="activity.time"
              placement="top"
            >
              <el-card shadow="never">
                <p>{{ activity.content }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="card">
          <div slot="header" class="card-header">
            <span>快速操作</span>
          </div>
          <div class="quick-actions">
            <el-button type="primary" icon="el-icon-plus" @click="$router.push('/datasource')">
              添加数据源
            </el-button>
            <el-button type="success" icon="el-icon-refresh" @click="$router.push('/table-select')">
              扫描表
            </el-button>
            <el-button type="warning" icon="el-icon-edit" @click="$router.push('/table-create')">
              在线建表
            </el-button>
            <el-button type="info" icon="el-icon-code" @click="$router.push('/script-editor')">
              脚本编辑
            </el-button>
          </div>
        </el-card>
        <el-card class="card" style="margin-top: 20px;">
          <div slot="header" class="card-header">
            <span>系统状态</span>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="运行环境">Spring Boot 2.7.x</el-descriptions-item>
            <el-descriptions-item label="数据库支持">MySQL, PostgreSQL</el-descriptions-item>
            <el-descriptions-item label="API 协议">RESTful, GraphQL</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {
        datasourceCount: 0,
        tableCount: 0,
        apiCount: 0,
        scriptCount: 0
      },
      recentActivities: []
    }
  },
  created() {
    this.loadDashboardData()
  },
  methods: {
    async loadDashboardData() {
      // 模拟数据
      this.stats = {
        datasourceCount: 2,
        tableCount: 15,
        apiCount: 12,
        scriptCount: 5
      }
      this.recentActivities = [
        { content: '新增数据源 "测试数据库"', time: '10分钟前' },
        { content: '生成 API 接口 "user"', time: '30分钟前' },
        { content: '更新脚本 "用户验证"', time: '1小时前' },
        { content: '热刷新 API 配置', time: '2小时前' }
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    .stat-icon {
      width: 50px;
      height: 50px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      float: left;

      i {
        font-size: 24px;
        color: #fff;
      }
    }

    .stat-info {
      margin-left: 70px;

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }

  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .el-button {
      flex: 1;
      min-width: 120px;
    }
  }
}
</style>

<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.datasourceCount }}</div>
            <div class="stat-label">数据源</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.tableCount }}</div>
            <div class="stat-label">数据表</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.apiCount }}</div>
            <div class="stat-label">API 接口</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.scriptCount }}</div>
            <div class="stat-label">脚本</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card class="card">
          <template #header>
            <span>最近活动</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in activities"
              :key="index"
              :timestamp="activity.time"
              :type="activity.type"
              placement="top"
            >
              <el-card shadow="never">
                <p>{{ activity.content }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="card">
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="quick-links">
            <div class="quick-link" @click="$router.push('/datasource')">
              <el-icon><Connection /></el-icon>
              <span>数据源管理</span>
            </div>
            <div class="quick-link" @click="$router.push('/table-select')">
              <el-icon><Grid /></el-icon>
              <span>表选择</span>
            </div>
            <div class="quick-link" @click="$router.push('/table-create')">
              <el-icon><Plus /></el-icon>
              <span>在线建表</span>
            </div>
            <div class="quick-link" @click="$router.push('/script-editor')">
              <el-icon><Edit /></el-icon>
              <span>脚本编辑</span>
            </div>
            <div class="quick-link" @click="$router.push('/api-docs')">
              <el-icon><Document /></el-icon>
              <span>API 文档</span>
            </div>
            <div class="quick-link" @click="$router.push('/api-test')">
              <el-icon><DataLine /></el-icon>
              <span>API 测试</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const stats = reactive({
  datasourceCount: 2,
  tableCount: 15,
  apiCount: 45,
  scriptCount: 8
})

const activities = ref([
  { time: '2024-01-20 10:30', content: '新增数据源 "测试数据库"', type: 'primary' },
  { time: '2024-01-19 15:20', content: '生成 API: /api/users', type: 'success' },
  { time: '2024-01-18 09:10', content: '执行建表脚本: user_profile', type: 'warning' },
  { time: '2024-01-17 14:00', content: '更新脚本: 用户验证逻辑', type: 'info' }
])
</script>

<style lang="scss" scoped>
.dashboard-container {
  .stat-card {
    display: flex;
    align-items: center;
    
    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 8px;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 24px;
      color: #fff;
      margin-right: 15px;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }
  
  .quick-links {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    
    .quick-link {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 10px;
      border: 1px solid #ebeef5;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background: #f5f7fa;
        border-color: #409eff;
        color: #409eff;
      }
      
      .el-icon {
        font-size: 24px;
        margin-bottom: 8px;
      }
      
      span {
        font-size: 12px;
      }
    }
  }
}
</style>

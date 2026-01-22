<template>
  <div class="api-docs-container">
    <el-card class="card">
      <template #header>
        <div class="card-header">
          <span>API 文档</span>
          <div>
            <el-button :icon="Document" @click="handleExport('openapi')">导出 OpenAPI</el-button>
            <el-button :icon="Document" @click="handleExport('swagger')">导出 Swagger</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="REST API" name="rest">
          <div class="api-list">
            <el-collapse v-model="expandedApi" accordion>
              <el-collapse-item
                v-for="api in apiList"
                :key="api.path"
                :name="api.path"
              >
                <template #title>
                  <div class="api-title">
                    <el-tag :type="getMethodTag(api.method)" size="small">{{ api.method }}</el-tag>
                    <span class="api-path">{{ api.path }}</span>
                    <span class="api-desc">{{ api.description }}</span>
                  </div>
                </template>
                <div class="api-detail">
                  <el-descriptions :column="1" border size="small">
                    <el-descriptions-item label="路径">{{ api.path }}</el-descriptions-item>
                    <el-descriptions-item label="方法">{{ api.method }}</el-descriptions-item>
                    <el-descriptions-item label="描述">{{ api.description }}</el-descriptions-item>
                    <el-descriptions-item label="请求参数">
                      <el-table :data="api.parameters" v-if="api.parameters?.length" border size="small">
                        <el-table-column prop="name" label="参数名" width="150" />
                        <el-table-column prop="type" label="类型" width="100" />
                        <el-table-column prop="required" label="必填" width="80">
                          <template #default="{ row }">
                            {{ row.required ? '是' : '否' }}
                          </template>
                        </el-table-column>
                        <el-table-column prop="description" label="描述" />
                      </el-table>
                      <span v-else class="text-info">无</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="响应示例">
                      <pre class="code-block">{{ api.responseExample }}</pre>
                    </el-descriptions-item>
                  </el-descriptions>
                  <div style="margin-top: 10px;">
                    <el-button type="primary" size="small" :icon="View" @click="handleTestApi(api)">测试</el-button>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>

        <el-tab-pane label="GraphQL" name="graphql">
          <div class="graphql-docs">
            <el-card class="sub-card">
              <template #header>Query（查询）</template>
              <pre class="code-block">{{ graphqlQueries }}</pre>
            </el-card>
            <el-card class="sub-card">
              <template #header>Mutation（变更）</template>
              <pre class="code-block">{{ graphqlMutations }}</pre>
            </el-card>
            <el-card class="sub-card">
              <template #header>类型定义</template>
              <pre class="code-block">{{ graphqlTypes }}</pre>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, View } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('rest')
const expandedApi = ref(null)

const apiList = ref([
  { path: '/api/users', method: 'GET', description: '获取用户列表（支持分页、筛选）',
    parameters: [
      { name: 'page', type: 'Integer', required: false, description: '页码' },
      { name: 'size', type: 'Integer', required: false, description: '每页数量' }
    ],
    responseExample: '{\n  "code": 0,\n  "data": {\n    "list": [...],\n    "total": 100\n  }\n}' },
  { path: '/api/users/{id}', method: 'GET', description: '获取单个用户',
    parameters: [{ name: 'id', type: 'Long', required: true, description: '用户ID' }],
    responseExample: '{\n  "code": 0,\n  "data": {\n    "id": 1,\n    "name": "张三"\n  }\n}' },
  { path: '/api/users', method: 'POST', description: '创建用户',
    parameters: [
      { name: 'name', type: 'String', required: true, description: '用户名' },
      { name: 'email', type: 'String', required: false, description: '邮箱' }
    ],
    responseExample: '{\n  "code": 0,\n  "data": {\n    "id": 1\n  }\n}' },
  { path: '/api/users/{id}', method: 'PUT', description: '更新用户',
    parameters: [{ name: 'id', type: 'Long', required: true, description: '用户ID' }],
    responseExample: '{\n  "code": 0\n}' },
  { path: '/api/users/{id}', method: 'DELETE', description: '删除用户',
    parameters: [{ name: 'id', type: 'Long', required: true, description: '用户ID' }],
    responseExample: '{\n  "code": 0\n}' }
])

const graphqlQueries = ref(`# 查询表列表
query Tables($datasourceId: ID!) {
  tables(datasourceId: $datasourceId) {
    name
    comment
  }
}`)

const graphqlMutations = ref(`# 创建数据
mutation CreateUser($input: UserInput!) {
  createUser(input: $input) {
    id
    name
  }
}`)

const graphqlTypes = ref(`type User {
  id: ID!
  name: String!
  email: String
}

input UserInput {
  name: String!
  email: String
}`)

function getMethodTag(method) {
  const tags = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return tags[method] || 'info'
}

function handleTabClick(tab) {
  console.log('切换到:', tab.name)
}

function handleExport(type) {
  ElMessage.success(`正在导出 ${type} 格式文档...`)
}

function handleTestApi(api) {
  router.push({ path: '/api-test', query: { path: api.path, method: api.method } })
}
</script>

<style lang="scss" scoped>
.api-docs-container {
  .api-title {
    display: flex;
    align-items: center;
    gap: 10px;

    .api-path {
      font-family: monospace;
      color: #303133;
    }

    .api-desc {
      color: #909399;
      font-size: 13px;
    }
  }

  .sub-card {
    margin-bottom: 20px;
  }
}
</style>
<template>
  <div class="api-test-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="card">
          <template #header>
            <span>API 列表</span>
          </template>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索 API"
            :prefix-icon="Search"
            clearable
            size="small"
          />
          <el-tree
            :data="apiTree"
            :props="{ label: 'label', children: 'children' }"
            :filter-node-method="filterNode"
            ref="apiTree"
            @node-click="handleNodeClick"
            style="margin-top: 10px;"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-tag :type="getMethodTag(data.method)" size="mini">{{ data.method }}</el-tag>
                <span class="tree-label">{{ data.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="card">
          <template #header>
            <span>请求</span>
            <el-button type="primary" :icon="Search" @click="handleSend" :loading="loading">发送请求</el-button>
          </template>

          <el-form label-width="80px" size="large">
            <el-form-item label="URL">
              <el-input v-model="request.url" placeholder="请输入请求 URL">
                <template #prepend>
                  <el-select v-model="request.method" style="width: 100px;">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </template>
              </el-input>
            </el-form-item>

            <el-tabs v-model="activeTab">
              <el-tab-pane label="Headers" name="headers">
                <el-table :data="request.headers" border size="small">
                  <el-table-column label="Key" width="200">
                    <template #default="{ row }">
                      <el-input v-model="row.key" placeholder="Key" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column label="Value">
                    <template #default="{ row }">
                      <el-input v-model="row.value" placeholder="Value" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80">
                    <template #default="{ row, $index }">
                      <el-button type="danger" :icon="Delete" size="small" @click="request.headers.splice($index, 1)" />
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size="small" :icon="Plus" @click="addHeader" style="margin-top: 10px;">添加 Header</el-button>
              </el-tab-pane>

              <el-tab-pane label="Params" name="params">
                <el-table :data="request.params" border size="small">
                  <el-table-column label="Key" width="200">
                    <template #default="{ row }">
                      <el-input v-model="row.key" placeholder="Key" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column label="Value">
                    <template #default="{ row }">
                      <el-input v-model="row.value" placeholder="Value" size="small" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80">
                    <template #default="{ row, $index }">
                      <el-button type="danger" :icon="Delete" size="small" @click="request.params.splice($index, 1)" />
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size="small" :icon="Plus" @click="addParam" style="margin-top: 10px;">添加 Param</el-button>
              </el-tab-pane>

              <el-tab-pane label="Body" name="body">
                <el-input
                  v-model="request.body"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入请求体（JSON 格式）"
                />
                <div style="margin-top: 10px;">
                  <el-button size="small" :icon="Document" @click="formatJson">格式化</el-button>
                  <el-button size="small" :icon="Delete" @click="clearBody">清空</el-button>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-form>
        </el-card>

        <el-card class="card" style="margin-top: 20px;">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>响应</span>
              <div>
                <span class="response-status" :class="response.status >= 200 && response.status < 300 ? 'success' : 'error'">
                  Status: {{ response.status || '-' }}
                </span>
                <span class="response-time" v-if="response.time">Time: {{ response.time }}ms</span>
              </div>
            </div>
          </template>

          <el-tabs v-model="responseTab">
            <el-tab-pane label="Body" name="body">
              <pre class="code-block">{{ response.body || '暂无响应' }}</pre>
            </el-tab-pane>
            <el-tab-pane label="Headers" name="headers">
              <el-table :data="responseHeaders" border size="small">
                <el-table-column prop="key" label="Key" width="200" />
                <el-table-column prop="value" label="Value" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Delete, Plus, Document } from '@element-plus/icons-vue'

const route = useRoute()
const apiTree = ref(null)
const loading = ref(false)

const searchForm = reactive({ keyword: '' })
const request = reactive({
  method: 'GET',
  url: '',
  headers: [{ key: 'Content-Type', value: 'application/json' }],
  params: [],
  body: ''
})
const response = reactive({
  status: null,
  time: null,
  body: '',
  headers: []
})
const activeTab = ref('params')
const responseTab = ref('body')

const apiTreeData = [
  { label: '用户管理', children: [
    { label: '获取用户列表', method: 'GET', path: '/api/users' },
    { label: '获取用户详情', method: 'GET', path: '/api/users/{id}' },
    { label: '创建用户', method: 'POST', path: '/api/users' },
    { label: '更新用户', method: 'PUT', path: '/api/users/{id}' },
    { label: '删除用户', method: 'DELETE', path: '/api/users/{id}' }
  ]},
  { label: '订单管理', children: [
    { label: '获取订单列表', method: 'GET', path: '/api/orders' },
    { label: '获取订单详情', method: 'GET', path: '/api/orders/{id}' }
  ]}
]

apiTree.value = apiTreeData

const responseHeaders = computed(() => {
  if (!response.headers?.length) {
    return [
      { key: 'Content-Type', value: 'application/json;charset=UTF-8' },
      { key: 'Date', value: new Date().toUTCString() }
    ]
  }
  return response.headers
})

watch(() => searchForm.keyword, (val) => {
  // 搜索过滤逻辑
})

if (route.query.path) {
  request.url = route.query.path
  request.method = route.query.method || 'GET'
}

function filterNode(value, data) {
  if (!value) return true
  return data.label?.toLowerCase().includes(value.toLowerCase())
}

function getMethodTag(method) {
  const tags = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return tags[method] || 'info'
}

function handleNodeClick(data) {
  if (data.path) {
    request.method = data.method
    request.url = data.path
  }
}

function addHeader() {
  request.headers.push({ key: '', value: '' })
}

function addParam() {
  request.params.push({ key: '', value: '' })
}

function formatJson() {
  try {
    const obj = JSON.parse(request.body)
    request.body = JSON.stringify(obj, null, 2)
  } catch (e) {
    ElMessage.error('JSON 格式错误')
  }
}

function clearBody() {
  request.body = ''
}

function handleSend() {
  if (!request.url) {
    ElMessage.warning('请输入请求 URL')
    return
  }
  
  loading.value = true
  const startTime = Date.now()
  
  setTimeout(() => {
    response.status = 200
    response.time = Date.now() - startTime
    response.body = JSON.stringify({
      code: 0,
      message: 'success',
      data: { list: [{ id: 1, name: '测试用户' }], total: 1 }
    }, null, 2)
    loading.value = false
    ElMessage.success('请求成功')
  }, 500)
}
</script>

<style lang="scss" scoped>
.api-test-container {
  .tree-node {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .tree-label {
    margin-left: 8px;
  }

  .response-status, .response-time {
    margin-left: 15px;
    font-size: 13px;
    color: #909399;
  }

  .response-status.success {
    color: #67c23a;
  }

  .response-status.error {
    color: #f56c6c;
  }
}
</style>

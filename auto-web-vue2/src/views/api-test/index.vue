<template>
  <div class="api-test-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="card">
          <div slot="header" class="card-header">
            <span>API 列表</span>
          </div>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索 API"
            prefix-icon="el-icon-search"
            clearable
            size="small"
          ></el-input>
          <el-tree
            :data="apiTree"
            :props="treeProps"
            :filter-node-method="filterNode"
            ref="apiTree"
            @node-click="handleNodeClick"
            style="margin-top: 10px;"
          >
            <span class="tree-node" slot-scope="{ node, data }">
              <el-tag :type="getMethodTag(data.method)" size="mini">{{ data.method }}</el-tag>
              <span class="tree-label">{{ data.label }}</span>
            </span>
          </el-tree>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="card">
          <div slot="header" class="card-header">
            <span>请求</span>
            <el-button type="primary" icon="el-icon-search" @click="handleSend" :loading="loading">发送请求</el-button>
          </div>

          <el-form label-width="80px" size="medium">
            <el-form-item label="URL">
              <el-input v-model="request.url" placeholder="请输入请求 URL">
                <el-select v-model="request.method" slot="prepend" style="width: 100px;">
                  <el-option label="GET" value="GET"></el-option>
                  <el-option label="POST" value="POST"></el-option>
                  <el-option label="PUT" value="PUT"></el-option>
                  <el-option label="DELETE" value="DELETE"></el-option>
                </el-select>
              </el-input>
            </el-form-item>

            <el-tabs v-model="activeTab">
              <el-tab-pane label="Headers" name="headers">
                <el-table :data="request.headers" border size="small">
                  <el-table-column label="Key" width="200">
                    <template slot-scope="{ row }">
                      <el-input v-model="row.key" placeholder="Key" size="small"></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column label="Value">
                    <template slot-scope="{ row }">
                      <el-input v-model="row.value" placeholder="Value" size="small"></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80">
                    <template slot-scope="{ row, $index }">
                      <el-button type="text" icon="el-icon-delete" @click="request.headers.splice($index, 1)" size="small"></el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size="small" icon="el-icon-plus" @click="addHeader" style="margin-top: 10px;">添加 Header</el-button>
              </el-tab-pane>

              <el-tab-pane label="Params" name="params">
                <el-table :data="request.params" border size="small">
                  <el-table-column label="Key" width="200">
                    <template slot-scope="{ row }">
                      <el-input v-model="row.key" placeholder="Key" size="small"></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column label="Value">
                    <template slot-scope="{ row }">
                      <el-input v-model="row.value" placeholder="Value" size="small"></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80">
                    <template slot-scope="{ row, $index }">
                      <el-button type="text" icon="el-icon-delete" @click="request.params.splice($index, 1)" size="small"></el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button size="small" icon="el-icon-plus" @click="addParam" style="margin-top: 10px;">添加 Param</el-button>
              </el-tab-pane>

              <el-tab-pane label="Body" name="body">
                <el-input
                  v-model="request.body"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入请求体（JSON 格式）"
                ></el-input>
                <div style="margin-top: 10px;">
                  <el-button size="small" icon="el-icon-document" @click="formatJson">格式化</el-button>
                  <el-button size="small" icon="el-icon-delete" @clear="clearBody">清空</el-button>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-form>
        </el-card>

        <el-card class="card" style="margin-top: 20px;">
          <div slot="header" class="card-header">
            <span>响应</span>
            <div>
              <span class="response-status" :class="response.status >= 200 && response.status < 300 ? 'success' : 'error'">
                Status: {{ response.status || '-' }}
              </span>
              <span class="response-time" v-if="response.time">Time: {{ response.time }}ms</span>
            </div>
          </div>

          <el-tabs v-model="responseTab">
            <el-tab-pane label="Body" name="body">
              <pre class="code-block" v-text="response.body || '暂无响应'"></pre>
            </el-tab-pane>
            <el-tab-pane label="Headers" name="headers">
              <el-table :data="responseHeaders" border size="small">
                <el-table-column prop="key" label="Key" width="200"></el-table-column>
                <el-table-column prop="value" label="Value"></el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'ApiTest',
  data() {
    return {
      searchForm: {
        keyword: ''
      },
      apiTree: [
        {
          label: '用户管理',
          children: [
            { label: '获取用户列表', method: 'GET', path: '/api/users' },
            { label: '获取用户详情', method: 'GET', path: '/api/users/{id}' },
            { label: '创建用户', method: 'POST', path: '/api/users' },
            { label: '更新用户', method: 'PUT', path: '/api/users/{id}' },
            { label: '删除用户', method: 'DELETE', path: '/api/users/{id}' }
          ]
        },
        {
          label: '订单管理',
          children: [
            { label: '获取订单列表', method: 'GET', path: '/api/orders' },
            { label: '获取订单详情', method: 'GET', path: '/api/orders/{id}' }
          ]
        }
      ],
      treeProps: { label: 'label', children: 'children' },
      request: {
        method: 'GET',
        url: '',
        headers: [{ key: 'Content-Type', value: 'application/json' }],
        params: [],
        body: ''
      },
      response: {
        status: null,
        time: null,
        body: '',
        headers: []
      },
      activeTab: 'params',
      responseTab: 'body',
      loading: false
    }
  },
  watch: {
    'searchForm.keyword'(val) {
      this.$refs.apiTree.filter(val)
    }
  },
  created() {
    if (this.$route.query.path) {
      this.request.url = this.$route.query.path
      this.request.method = this.$route.query.method || 'GET'
    }
  },
  computed: {
    responseHeaders() {
      if (!this.response.headers || this.response.headers.length === 0) {
        return [
          { key: 'Content-Type', value: 'application/json;charset=UTF-8' },
          { key: 'Date', value: new Date().toUTCString() }
        ]
      }
      return this.response.headers
    }
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.label.toLowerCase().indexOf(value.toLowerCase()) !== -1
    },
    getMethodTag(method) {
      const tags = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
      return tags[method] || 'info'
    },
    handleNodeClick(data) {
      if (data.path) {
        this.request.method = data.method
        this.request.url = data.path
      }
    },
    addHeader() {
      this.request.headers.push({ key: '', value: '' })
    },
    addParam() {
      this.request.params.push({ key: '', value: '' })
    },
    formatJson() {
      try {
        const obj = JSON.parse(this.request.body)
        this.request.body = JSON.stringify(obj, null, 2)
      } catch (e) {
        this.$message.error('JSON 格式错误')
      }
    },
    clearBody() {
      this.request.body = ''
    },
    handleSend() {
      if (!this.request.url) {
        this.$message.warning('请输入请求 URL')
        return
      }
      
      this.loading = true
      const startTime = Date.now()
      
      setTimeout(() => {
        this.response.status = 200
        this.response.time = Date.now() - startTime
        this.response.body = JSON.stringify({
          code: 0,
          message: 'success',
          data: {
            list: [
              { id: 1, name: '测试用户', email: 'test@example.com' },
              { id: 2, name: '示例用户', email: 'example@example.com' }
            ],
            total: 2,
            page: 1,
            size: 10
          }
        }, null, 2)
        this.loading = false
        this.$message.success('请求成功')
      }, 500)
    }
  }
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

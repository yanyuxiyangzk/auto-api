<template>
  <div class="api-docs-container">
    <el-card class="card">
      <div slot="header" class="card-header">
        <span>API 文档</span>
        <div>
          <el-button icon="el-icon-document" @click="handleExport('openapi')">导出 OpenAPI</el-button>
          <el-button icon="el-icon-document" @click="handleExport('swagger')">导出 Swagger</el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="REST API" name="rest">
          <div class="api-list">
            <el-collapse v-model="expandedApi" accordion>
              <el-collapse-item
                v-for="api in apiList"
                :key="api.path"
                :name="api.path"
              >
                <template slot="title">
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
                      <el-table :data="api.parameters" v-if="api.parameters && api.parameters.length" border size="small">
                        <el-table-column prop="name" label="参数名" width="150"></el-table-column>
                        <el-table-column prop="type" label="类型" width="100"></el-table-column>
                        <el-table-column prop="required" label="必填" width="80">
                          <template slot-scope="{ row }">
                            {{ row.required ? '是' : '否' }}
                          </template>
                        </el-table-column>
                        <el-table-column prop="description" label="描述"></el-table-column>
                      </el-table>
                      <span v-else class="text-info">无</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="响应示例">
                      <pre class="code-block">{{ api.responseExample }}</pre>
                    </el-descriptions-item>
                  </el-descriptions>
                  <div style="margin-top: 10px;">
                    <el-button type="primary" size="small" icon="el-icon-view" @click="handleTestApi(api)">测试</el-button>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>

        <el-tab-pane label="GraphQL" name="graphql">
          <div class="graphql-docs">
            <el-card class="sub-card">
              <div slot="header">Query（查询）</div>
              <div class="code-block" v-text="graphqlQueries"></div>
            </el-card>
            <el-card class="sub-card">
              <div slot="header">Mutation（变更）</div>
              <div class="code-block" v-text="graphqlMutations"></div>
            </el-card>
            <el-card class="sub-card">
              <div slot="header">类型定义</div>
              <div class="code-block" v-text="graphqlTypes"></div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ApiDocs',
  data() {
    return {
      activeTab: 'rest',
      expandedApi: null,
      apiList: []
    }
  },
  computed: {
    graphqlQueries() {
      return `# 查询表列表
query Tables($datasourceId: ID!) {
  tables(datasourceId: $datasourceId) {
    name
    comment
    columns {
      name
      type
      comment
    }
  }
}

# 查询单条数据
query Table($datasourceId: ID!, $name: String!) {
  table(datasourceId: $datasourceId, name: $name) {
    name
    comment
    columns {
      name
      type
      comment
      primaryKey
    }
  }
}`
    },
    graphqlMutations() {
      return `# 创建数据
mutation CreateUser($input: UserInput!) {
  createUser(input: $input) {
    id
    name
    email
  }
}

# 更新数据
mutation UpdateUser($id: ID!, $input: UserInput!) {
  updateUser(id: $id, input: $input) {
    success
  }
}

# 删除数据
mutation DeleteUser($id: ID!) {
  deleteUser(id: $id) {
    success
  }
}`
    },
    graphqlTypes() {
      return `type User {
  id: ID!
  name: String!
  email: String
  createdAt: String
}

input UserInput {
  name: String!
  email: String
}

type Query {
  users: [User!]!
  user(id: ID!): User
}

type Mutation {
  createUser(input: UserInput!): User!
  updateUser(id: ID!, input: UserInput!): Boolean!
  deleteUser(id: ID!): Boolean!
}`
    }
  },
  created() {
    this.loadApiDocs()
  },
  methods: {
    async loadApiDocs() {
      this.apiList = [
        {
          path: '/api/users',
          method: 'GET',
          description: '获取用户列表（支持分页、筛选）',
          parameters: [
            { name: 'page', type: 'Integer', required: false, description: '页码' },
            { name: 'size', type: 'Integer', required: false, description: '每页数量' },
            { name: 'orderBy', type: 'String', required: false, description: '排序字段' },
            { name: 'order', type: 'String', required: false, description: '排序方向 ASC/DESC' }
          ],
          responseExample: '{\n  "code": 0,\n  "data": {\n    "list": [...],\n    "total": 100\n  }\n}'
        },
        {
          path: '/api/users/{id}',
          method: 'GET',
          description: '获取单个用户',
          parameters: [
            { name: 'id', type: 'Long', required: true, description: '用户ID' }
          ],
          responseExample: '{\n  "code": 0,\n  "data": {\n    "id": 1,\n    "name": "张三",\n    "email": "zhangsan@example.com"\n  }\n}'
        },
        {
          path: '/api/users',
          method: 'POST',
          description: '创建用户',
          parameters: [
            { name: 'name', type: 'String', required: true, description: '用户名' },
            { name: 'email', type: 'String', required: false, description: '邮箱' }
          ],
          responseExample: '{\n  "code": 0,\n  "data": {\n    "id": 1,\n    "name": "张三"\n  }\n}'
        },
        {
          path: '/api/users/{id}',
          method: 'PUT',
          description: '更新用户',
          parameters: [
            { name: 'id', type: 'Long', required: true, description: '用户ID' },
            { name: 'name', type: 'String', required: false, description: '用户名' },
            { name: 'email', type: 'String', required: false, description: '邮箱' }
          ],
          responseExample: '{\n  "code": 0\n}'
        },
        {
          path: '/api/users/{id}',
          method: 'DELETE',
          description: '删除用户',
          parameters: [
            { name: 'id', type: 'Long', required: true, description: '用户ID' }
          ],
          responseExample: '{\n  "code": 0\n}'
        }
      ]
    },
    getMethodTag(method) {
      const tags = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
      return tags[method] || 'info'
    },
    handleTabClick(tab) {
      console.log('切换到:', tab.name)
    },
    handleExport(type) {
      this.$message.success(`正在导出 ${type} 格式文档...`)
    },
    handleTestApi(api) {
      this.$router.push({ path: '/api-test', query: { path: api.path, method: api.method } })
    }
  }
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
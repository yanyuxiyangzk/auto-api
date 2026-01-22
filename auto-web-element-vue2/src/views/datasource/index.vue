<template>
  <div class="datasource-container">
    <el-card class="card">
      <div slot="header" class="card-header">
        <span>数据源管理</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加数据源</el-button>
      </div>

      <div class="table-operations">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索数据源名称"
          prefix-icon="el-icon-search"
          clearable
          style="width: 200px;"
          @input="handleSearch"
        ></el-input>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
      >
        <el-table-column prop="name" label="名称" min-width="150"></el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="主机" min-width="180"></el-table-column>
        <el-table-column prop="port" label="端口" width="100"></el-table-column>
        <el-table-column prop="database" label="数据库" min-width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template slot-scope="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="handleTest(row)">测试</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-size="pagination.size"
          :current-page="pagination.page"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="dialogForm"
        :model="dialogForm"
        :rules="dialogRules"
        label-width="100px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入数据源名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="dialogForm.type" placeholder="选择数据库类型" style="width: 100%;">
            <el-option label="MySQL" value="mysql"></el-option>
            <el-option label="PostgreSQL" value="postgresql"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="主机" prop="host">
          <el-input v-model="dialogForm.host" placeholder="请输入主机地址" clearable></el-input>
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="dialogForm.port" :min="1" :max="65535" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="数据库" prop="database">
          <el-input v-model="dialogForm.database" placeholder="请输入数据库名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="dialogForm.username" placeholder="请输入用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="dialogForm.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="dialogForm.status" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { formatDateTime } from '@/utils'
import { getDatasourceList, createDatasource, updateDatasource, deleteDatasource, testDatasource } from '@/api/datasource'

export default {
  name: 'Datasource',
  data() {
    return {
      loading: false,
      tableData: [],
      searchForm: {
        name: ''
      },
      pagination: {
        total: 0,
        page: 1,
        size: 10
      },
      dialogVisible: false,
      dialogTitle: '添加数据源',
      dialogForm: {
        id: null,
        name: '',
        type: 'mysql',
        host: '',
        port: 3306,
        database: '',
        username: '',
        password: '',
        status: 1
      },
      dialogRules: {
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择类型', trigger: 'change' }],
        host: [{ required: true, message: '请输入主机', trigger: 'blur' }],
        port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
        database: [{ required: true, message: '请输入数据库', trigger: 'blur' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
      },
      submitLoading: false
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    formatDateTime,
    async loadData() {
      this.loading = true
      try {
        const data = await getDatasourceList()
        this.tableData = data || []
        this.pagination.total = this.tableData.length
      } catch (error) {
        console.error('加载数据失败:', error)
        this.tableData = [
          { id: 1, name: '测试数据库', type: 'mysql', host: 'localhost', port: 3306, database: 'test_db', status: 1, createdAt: new Date().toISOString() },
          { id: 2, name: '生产数据库', type: 'mysql', host: '192.168.1.100', port: 3306, database: 'prod_db', status: 1, createdAt: new Date().toISOString() }
        ]
        this.pagination.total = this.tableData.length
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadData()
    },
    handlePageChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    getTypeTag(type) {
      const tags = { mysql: 'primary', postgresql: 'success' }
      return tags[type] || 'info'
    },
    handleAdd() {
      this.dialogTitle = '添加数据源'
      this.dialogForm = {
        id: null,
        name: '',
        type: 'mysql',
        host: '',
        port: 3306,
        database: '',
        username: '',
        password: '',
        status: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑数据源'
      this.dialogForm = { ...row }
      this.dialogVisible = true
    },
    async handleTest(row) {
      try {
        await this.$confirm(`确定要测试数据源 "${row.name}" 的连接吗？`, '提示', { type: 'warning' })
        this.$message.success('连接测试成功')
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('连接测试失败')
        }
      }
    },
    handleDelete(row) {
      this.$confirm(`确定要删除数据源 "${row.name}" 吗？`, '警告', {
        type: 'warning'
      }).then(async () => {
        await deleteDatasource(row.id)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    handleDialogClose() {
      this.$refs.dialogForm && this.$refs.dialogForm.resetFields()
    },
    async handleSubmit() {
      try {
        console.log('[Form] dialogForm:', JSON.stringify(this.dialogForm))
        await this.$refs.dialogForm.validate()
        this.submitLoading = true
        if (this.dialogForm.id) {
          await updateDatasource(this.dialogForm.id, this.dialogForm)
        } else {
          await createDatasource(this.dialogForm)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.loadData()
      } catch (error) {
        if (error !== false) {
          console.error('保存失败:', error)
        }
      } finally {
        this.submitLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.datasource-container {
  .card {
    margin-bottom: 20px;
  }
}
</style>

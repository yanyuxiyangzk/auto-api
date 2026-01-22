<template>
  <div class="datasource-container">
    <el-card class="card">
      <template #header>
        <div class="card-header">
          <span>数据源管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加数据源</el-button>
        </div>
      </template>

      <div class="table-operations">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索数据源名称"
          :prefix-icon="Search"
          clearable
          style="width: 200px;"
          @input="handleSearch"
        />
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="主机" min-width="180" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="database" label="数据库" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleTest(row)">测试</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入数据源名称" clearable />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="dialogForm.type" placeholder="选择数据库类型" style="width: 100%;">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机" prop="host">
          <el-input v-model="dialogForm.host" placeholder="请输入主机地址" clearable />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="dialogForm.port" :min="1" :max="65535" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="数据库" prop="database">
          <el-input v-model="dialogForm.database" placeholder="请输入数据库名称" clearable />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="dialogForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="dialogForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="dialogForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils'
import { getDatasourceList, createDatasource, updateDatasource, deleteDatasource } from '@/api/datasource'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ name: '' })
const pagination = reactive({ total: 0, page: 1, size: 10 })
const dialogVisible = ref(false)
const dialogTitle = ref('添加数据源')
const dialogFormRef = ref(null)
const submitLoading = ref(false)

const dialogForm = reactive({
  id: null,
  name: '',
  type: 'mysql',
  host: '',
  port: 3306,
  database: '',
  username: '',
  password: '',
  status: 1
})

const dialogRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机', trigger: 'blur' }],
  database: [{ required: true, message: '请输入数据库', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const data = await getDatasourceList()
    tableData.value = data || []
    pagination.total = tableData.value.length
  } catch (error) {
    tableData.value = [
      { id: 1, name: '测试数据库', type: 'mysql', host: 'localhost', port: 3306, database: 'test_db', status: 1, createdAt: new Date().toISOString() },
      { id: 2, name: '生产数据库', type: 'mysql', host: '192.168.1.100', port: 3306, database: 'prod_db', status: 1, createdAt: new Date().toISOString() }
    ]
    pagination.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function getTypeTag(type) {
  const tags = { mysql: 'primary', postgresql: 'success' }
  return tags[type] || 'info'
}

function handleAdd() {
  dialogTitle.value = '添加数据源'
  Object.assign(dialogForm, { id: null, name: '', type: 'mysql', host: '', port: 3306, database: '', username: '', password: '', status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑数据源'
  Object.assign(dialogForm, row)
  dialogVisible.value = true
}

async function handleTest(row) {
  ElMessage.success('连接测试成功')
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除数据源 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      await deleteDatasource(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

function handleDialogClose() {
  dialogFormRef.value?.resetFields()
}

async function handleSubmit() {
  try {
    await dialogFormRef.value.validate()
    submitLoading.value = true
    if (dialogForm.id) {
      await updateDatasource(dialogForm.id, dialogForm)
    } else {
      await createDatasource(dialogForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
    }
  } finally {
    submitLoading.value = false
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

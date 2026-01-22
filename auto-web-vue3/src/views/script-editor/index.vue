<template>
  <div class="script-editor-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="card">
          <template #header>
            <div class="card-header">
              <span>脚本列表</span>
              <el-button type="primary" :icon="Plus" size="small" @click="handleCreate">新建</el-button>
            </div>
          </template>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索脚本"
            :prefix-icon="Search"
            clearable
            size="small"
            @input="handleSearch"
          />
          <el-menu :default-active="activeScriptId" @select="handleScriptSelect" style="border: none; margin-top: 10px;">
            <el-menu-item v-for="script in scriptList" :key="script.id" :index="String(script.id)">
              <span>{{ script.name }}</span>
              <el-tag size="mini" :type="script.status === 1 ? 'success' : 'info'" style="margin-left: auto;">
                {{ script.status === 1 ? '已发布' : '草稿' }}
              </el-tag>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card class="card" v-if="currentScript">
          <template #header>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-input v-model="currentScript.name" placeholder="脚本名称" style="width: 200px;" size="small" />
              <el-select v-model="currentScript.type" placeholder="脚本类型" size="small" style="width: 150px;">
                <el-option label="拦截器" value="interceptor" />
                <el-option label="覆盖逻辑" value="override" />
                <el-option label="自定义" value="custom" />
              </el-select>
              <el-input v-model="currentScript.targetTable" placeholder="目标表（可选）" style="width: 150px;" size="small" />
            </div>
            <div>
              <el-button :icon="Document" size="small" @click="handleSaveVersion">保存版本</el-button>
              <el-button :icon="VideoPlay" size="small" @click="handleTest">测试执行</el-button>
              <el-button type="primary" :icon="Check" size="small" @click="handlePublish" :disabled="currentScript.status === 1">
                发布
              </el-button>
            </div>
          </template>

          <el-input
            v-model="currentScript.content"
            type="textarea"
            :rows="15"
            placeholder="在此输入 Groovy 脚本代码"
            style="font-family: monospace;"
          />

          <div class="script-info" style="margin-top: 15px;">
            <el-descriptions :column="4" border size="small">
              <el-descriptions-item label="版本">v{{ currentScript.version }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(currentScript.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatDateTime(currentScript.updatedAt) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="currentScript.status === 1 ? 'success' : 'info'" size="small">
                  {{ currentScript.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <el-card class="card" v-if="currentScript" style="margin-top: 20px;">
          <template #header>
            <span>版本历史</span>
          </template>
          <el-table :data="versionList" border size="small">
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column prop="changeLog" label="变更说明" min-width="200" />
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="handleRollback(row)">回滚</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="card" v-if="currentScript" style="margin-top: 20px;">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>执行日志</span>
              <el-button type="text" :icon="Delete" size="small" @click="clearLogs">清空</el-button>
            </div>
          </template>
          <div class="execution-logs" v-html="executionLogs" />
        </el-card>

        <el-card class="card" v-if="!currentScript">
          <div class="empty-state">
            <el-icon class="empty-icon" :size="64"><Document /></el-icon>
            <p class="empty-text">请选择一个脚本或创建新脚本</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Document, VideoPlay, Check, Delete } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils'
import { getScriptList } from '@/api/script'

const searchForm = reactive({ keyword: '' })
const scriptList = ref([])
const activeScriptId = ref(null)
const currentScript = ref(null)
const versionList = ref([])
const executionLogs = ref('')

onMounted(() => {
  loadScripts()
})

async function loadScripts() {
  try {
    const data = await getScriptList()
    scriptList.value = data || getMockScripts()
  } catch (error) {
    scriptList.value = getMockScripts()
  }
}

function getMockScripts() {
  return [
    { id: 1, name: '用户验证脚本', type: 'interceptor', targetTable: 'users', status: 1, version: 2, 
      content: '// 用户验证脚本\ndef userId = params.get("userId");\nif (!userId) {\n    throw new RuntimeException("用户ID不能为空");\n}',
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
    { id: 2, name: '订单计算', type: 'override', targetTable: 'orders', status: 0, version: 1, 
      content: '// 订单计算逻辑\ndef amount = params.get("amount");\ndef tax = amount * 0.1;\nreturn [amount: amount, tax: tax];',
      createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }
  ]
}

function handleSearch() {}

function handleCreate() {
  const newScript = {
    id: Date.now(),
    name: '新建脚本',
    type: 'custom',
    targetTable: '',
    status: 0,
    version: 1,
    content: '// 在这里编写 Groovy 脚本\n\n// 可以使用以下变量:\n// - params: 请求参数\n// - request: 请求对象\n// - response: 响应对象\n// - db: 数据库操作对象\n\nreturn params;',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  scriptList.value.unshift(newScript)
  activeScriptId.value = String(newScript.id)
  currentScript.value = newScript
}

function handleScriptSelect(id) {
  activeScriptId.value = id
  currentScript.value = scriptList.value.find(s => s.id === Number(id))
  versionList.value = [
    { version: 2, changeLog: '优化验证逻辑', createdAt: new Date().toISOString() },
    { version: 1, changeLog: '初始版本', createdAt: new Date(Date.now() - 86400000).toISOString() }
  ]
}

function handleSaveVersion() {
  ElMessage.success('保存成功')
}

function handleTest() {
  executionLogs.value += `<div style="color: #909399;">[${new Date().toLocaleTimeString()}] 开始执行...</div>`
  setTimeout(() => {
    executionLogs.value += `<div style="color: #67c23a;">[${new Date().toLocaleTimeString()}] 执行成功</div>`
  }, 500)
}

function handlePublish() {
  currentScript.value.status = 1
  ElMessage.success('发布成功')
}

function handleRollback(version) {
  ElMessageBox.confirm(`确定要回滚到版本 v${version.version} 吗？`, '提示', { type: 'warning' })
    .then(() => { ElMessage.success('回滚成功') })
    .catch(() => {})
}

function clearLogs() {
  executionLogs.value = ''
}
</script>

<style lang="scss" scoped>
.script-editor-container {
  .execution-logs {
    max-height: 200px;
    overflow-y: auto;
    background: #282c34;
    color: #abb2bf;
    padding: 10px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 12px;
  }
}
</style>

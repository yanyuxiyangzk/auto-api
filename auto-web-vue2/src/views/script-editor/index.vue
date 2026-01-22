<template>
  <div class="script-editor-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="card">
          <div slot="header" class="card-header">
            <span>脚本列表</span>
            <el-button type="primary" icon="el-icon-plus" size="small" @click="handleCreate">新建</el-button>
          </div>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索脚本"
            prefix-icon="el-icon-search"
            clearable
            size="small"
            @input="handleSearch"
          ></el-input>
          <el-menu
            :default-active="activeScriptId"
            @select="handleScriptSelect"
            style="border: none; margin-top: 10px;"
          >
            <el-menu-item v-for="script in scriptList" :key="script.id" :index="String(script.id)">
              <i :class="getScriptIcon(script.type)"></i>
              <span slot="title">{{ script.name }}</span>
              <el-tag size="mini" :type="script.status === 1 ? 'success' : 'info'" style="margin-left: auto;">
                {{ script.status === 1 ? '已发布' : '草稿' }}
              </el-tag>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card class="card" v-if="currentScript">
          <div slot="header" class="card-header">
            <div style="display: flex; align-items: center;">
              <el-input
                v-model="currentScript.name"
                placeholder="脚本名称"
                style="width: 200px; margin-right: 10px;"
                size="small"
              ></el-input>
              <el-select v-model="currentScript.type" placeholder="脚本类型" size="small" style="width: 150px;">
                <el-option label="拦截器" value="interceptor"></el-option>
                <el-option label="覆盖逻辑" value="override"></el-option>
                <el-option label="自定义" value="custom"></el-option>
              </el-select>
              <el-input
                v-model="currentScript.targetTable"
                placeholder="目标表（可选）"
                style="width: 150px; margin-left: 10px;"
                size="small"
              ></el-input>
            </div>
            <div>
              <el-button icon="el-icon-document" size="small" @click="handleSaveVersion">保存版本</el-button>
              <el-button icon="el-icon-refresh" size="small" @click="handleTest">测试执行</el-button>
              <el-button type="primary" icon="el-icon-check" size="small" @click="handlePublish" :disabled="currentScript.status === 1">
                发布
              </el-button>
            </div>
          </div>

          <codemirror
            v-model="currentScript.content"
            :options="editorOptions"
            style="height: 400px; border: 1px solid #dcdfe6;"
          ></codemirror>

          <div class="script-info">
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

        <el-card class="card" v-if="currentScript">
          <div slot="header" class="card-header">
            <span>版本历史</span>
          </div>
          <el-table :data="versionList" border size="small">
            <el-table-column prop="version" label="版本" width="80"></el-table-column>
            <el-table-column prop="changeLog" label="变更说明" min-width="200"></el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template slot-scope="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="{ row }">
                <el-button type="text" size="small" @click="handleRollback(row)">回滚</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="card" v-if="currentScript">
          <div slot="header" class="card-header">
            <span>执行日志</span>
            <el-button type="text" size="small" icon="el-icon-delete" @click="clearLogs">清空</el-button>
          </div>
          <div class="execution-logs" v-html="executionLogs"></div>
        </el-card>

        <el-card class="card" v-if="!currentScript">
          <div class="empty-state">
            <i class="el-icon-document empty-icon"></i>
            <p class="empty-text">请选择一个脚本或创建新脚本</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { formatDateTime } from '@/utils'
import { getScriptList, createScript, updateScript, executeScript, getScriptVersions, rollbackScript } from '@/api/script'

export default {
  name: 'ScriptEditor',
  data() {
    return {
      searchForm: {
        keyword: ''
      },
      scriptList: [],
      activeScriptId: null,
      currentScript: null,
      versionList: [],
      executionLogs: '',
      editorOptions: {
        mode: 'text/x-groovy',
        theme: 'dracula',
        lineNumbers: true,
        indentUnit: 4,
        tabSize: 4,
        lineWrapping: true,
        foldGutter: true,
        gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter', 'CodeMirror-markergutter']
      }
    }
  },
  created() {
    this.loadScripts()
  },
  methods: {
    formatDateTime,
    async loadScripts() {
      try {
        const data = await getScriptList()
        this.scriptList = data || this.getMockScripts()
      } catch (error) {
        this.scriptList = this.getMockScripts()
      }
    },
    getMockScripts() {
      return [
        { id: 1, name: '用户验证脚本', type: 'interceptor', targetTable: 'users', status: 1, version: 2, content: '// 用户验证脚本\ndef userId = params.get("userId");\nif (!userId) {\n    throw new RuntimeException("用户ID不能为空");\n}', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
        { id: 2, name: '订单计算', type: 'override', targetTable: 'orders', status: 0, version: 1, content: '// 订单计算逻辑\ndef amount = params.get("amount");\ndef tax = amount * 0.1;\nreturn [amount: amount, tax: tax];', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() }
      ]
    },
    handleSearch() {
      // 搜索逻辑
    },
    handleCreate() {
      const newScript = {
        id: Date.now(),
        name: '新建脚本',
        type: 'custom',
        targetTable: '',
        status: 0,
        version: 1,
        content: `// 在这里编写 Groovy 脚本\n\n// 可以使用以下变量:\n// - params: 请求参数\n// - request: 请求对象\n// - response: 响应对象\n// - db: 数据库操作对象\n\nreturn params;`,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      this.scriptList.unshift(newScript)
      this.activeScriptId = String(newScript.id)
      this.currentScript = newScript
    },
    handleScriptSelect(id) {
      this.activeScriptId = id
      this.currentScript = this.scriptList.find(s => s.id === Number(id))
      this.loadVersions(this.currentScript.id)
    },
    async loadVersions(scriptId) {
      this.versionList = [
        { version: 2, changeLog: '优化验证逻辑', createdAt: new Date().toISOString() },
        { version: 1, changeLog: '初始版本', createdAt: new Date(Date.now() - 86400000).toISOString() }
      ]
    },
    async handleSaveVersion() {
      this.$message.success('保存成功')
    },
    handleTest() {
      this.executionLogs += `<div style="color: #909399;">[${new Date().toLocaleTimeString()}] 开始执行...</div>`
      setTimeout(() => {
        this.executionLogs += `<div style="color: #67c23a;">[${new Date().toLocaleTimeString()}] 执行成功，返回: ${JSON.stringify({ result: 'success' })}</div>`
      }, 500)
    },
    handlePublish() {
      this.currentScript.status = 1
      this.$message.success('发布成功')
    },
    handleRollback(version) {
      this.$confirm(`确定要回滚到版本 v${version.version} 吗？`, '提示', { type: 'warning' })
        .then(() => {
          this.$message.success('回滚成功')
        })
        .catch(() => {})
    },
    getScriptIcon(type) {
      const icons = { interceptor: 'el-icon-lock', override: 'el-icon-edit-outline', custom: 'el-icon-code' }
      return icons[type] || 'el-icon-document'
    },
    clearLogs() {
      this.executionLogs = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.script-editor-container {
  .script-info {
    margin-top: 15px;
  }

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
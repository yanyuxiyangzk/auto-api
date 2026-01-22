<template>
  <div class="table-create-container">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card class="card">
          <template #header>
            <div class="card-header">
              <span>在线建表</span>
              <div>
                <el-button :icon="Document" @click="handlePreviewDDL">预览 DDL</el-button>
                <el-button type="primary" :icon="Check" @click="handleExecute" :loading="executing">
                  执行建表
                </el-button>
              </div>
            </div>
          </template>

          <el-form :model="tableForm" label-width="100px" size="large">
            <el-form-item label="表名" required>
              <el-input v-model="tableForm.tableName" placeholder="请输入表名（小写字母、下划线）" clearable />
            </el-form-item>
            <el-form-item label="注释">
              <el-input v-model="tableForm.tableComment" placeholder="请输入表注释" />
            </el-form-item>

            <el-divider>字段定义</el-divider>

            <el-table :data="tableForm.columns" border stripe>
              <el-table-column label="字段名" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.name" placeholder="字段名" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="类型" width="180">
                <template #default="{ row }">
                  <el-select v-model="row.type" placeholder="选择类型" size="small" style="width: 100%;">
                    <el-option-group label="整数">
                      <el-option label="TINYINT" value="TINYINT" />
                      <el-option label="SMALLINT" value="SMALLINT" />
                      <el-option label="INT" value="INT" />
                      <el-option label="BIGINT" value="BIGINT" />
                    </el-option-group>
                    <el-option-group label="字符串">
                      <el-option label="VARCHAR(255)" value="VARCHAR(255)" />
                      <el-option label="TEXT" value="TEXT" />
                    </el-option-group>
                    <el-option-group label="日期时间">
                      <el-option label="DATE" value="DATE" />
                      <el-option label="DATETIME" value="DATETIME" />
                    </el-option-group>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="注释">
                <template #default="{ row }">
                  <el-input v-model="row.comment" placeholder="注释" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="主键" width="70" align="center">
                <template #default="{ row }">
                  <el-checkbox v-model="row.primaryKey" />
                </template>
              </el-table-column>
              <el-table-column label="可空" width="70" align="center">
                <template #default="{ row }">
                  <el-checkbox v-model="row.nullable" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ row, $index }">
                  <el-button type="danger" :icon="Delete" size="small" @click="tableForm.columns.splice($index, 1)" />
                </template>
              </el-table-column>
            </el-table>

            <div style="margin-top: 15px;">
              <el-button :icon="Plus" @click="handleAddColumn">添加字段</el-button>
            </div>

            <el-divider>高级选项</el-divider>

            <el-form-item label="数据源">
              <el-select v-model="tableForm.datasourceId" placeholder="选择数据源" style="width: 300px;">
                <el-option label="测试数据库" value="1" />
                <el-option label="生产数据库" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="建表后操作">
              <el-checkbox-group v-model="tableForm.afterActions">
                <el-checkbox label="generate_api">自动生成 API</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="card">
          <template #header>
            <span>模板</span>
          </template>
          <el-button
            v-for="template in templates"
            :key="template.name"
            type="text"
            @click="applyTemplate(template)"
            style="display: block; margin: 10px 0;"
          >
            {{ template.name }}
          </el-button>
        </el-card>

        <el-card class="card" style="margin-top: 20px;">
          <template #header>
            <span>SQL 预览</span>
          </template>
          <pre class="code-block">{{ previewSQL }}</pre>
        </el-card>

        <el-card class="card" style="margin-top: 20px;">
          <template #header>
            <span>建表历史</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(history, index) in historyList"
              :key="index"
              :timestamp="history.time"
              placement="top"
            >
              <el-card shadow="never">
                <p><strong>{{ history.tableName }}</strong></p>
                <p class="text-info">{{ history.status }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预览对话框 -->
    <el-dialog title="DDL 预览" v-model="previewVisible" width="700px">
      <pre class="code-block">{{ previewSQL }}</pre>
      <template #footer>
        <el-button @click="previewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExecute" :loading="executing">执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Check, Delete, Plus } from '@element-plus/icons-vue'

const executing = ref(false)
const previewVisible = ref(false)
const tableForm = reactive({
  tableName: '',
  tableComment: '',
  datasourceId: '1',
  columns: [
    { name: 'id', type: 'BIGINT', comment: '主键ID', primaryKey: true, nullable: false },
    { name: 'created_at', type: 'DATETIME', comment: '创建时间', primaryKey: false, nullable: false },
    { name: 'updated_at', type: 'DATETIME', comment: '更新时间', primaryKey: false, nullable: true }
  ],
  afterActions: []
})

const templates = [
  { name: '用户表', template: { tableName: 'sys_user', tableComment: '用户表', columns: [
    { name: 'id', type: 'BIGINT', comment: '主键', primaryKey: true },
    { name: 'username', type: 'VARCHAR(50)', comment: '用户名', nullable: false },
    { name: 'email', type: 'VARCHAR(100)', comment: '邮箱' }
  ]}},
  { name: '订单表', template: { tableName: 'biz_order', tableComment: '订单表', columns: [
    { name: 'id', type: 'BIGINT', comment: '主键', primaryKey: true },
    { name: 'order_no', type: 'VARCHAR(50)', comment: '订单号', nullable: false },
    { name: 'amount', type: 'DECIMAL(10,2)', comment: '金额' }
  ]}}
]

const historyList = ref([
  { tableName: 'test_table', time: '2024-01-20 10:30', status: '执行成功' },
  { tableName: 'user_profile', time: '2024-01-19 15:20', status: '执行成功' }
])

const previewSQL = computed(() => {
  const { tableName, tableComment, columns } = tableForm
  if (!tableName) return '-- 请输入表名'
  
  let sql = `CREATE TABLE \`${tableName}\` (\n`
  const columnDefs = columns.map(col => {
    let definition = `  \`${col.name}\` ${col.type}`
    if (!col.nullable) definition += ' NOT NULL'
    if (col.primaryKey) definition += ' AUTO_INCREMENT'
    if (col.comment) definition += ` COMMENT '${col.comment}'`
    return definition
  })
  
  sql += columnDefs.join(',\n')
  sql += '\n)'
  if (tableComment) sql += ` COMMENT='${tableComment}'`
  sql += ';'
  
  return sql
})

function handleAddColumn() {
  tableForm.columns.push({ name: '', type: 'VARCHAR(255)', comment: '', primaryKey: false, nullable: true })
}

function applyTemplate(template) {
  const t = template.template
  tableForm.tableName = t.tableName
  tableForm.tableComment = t.tableComment
  tableForm.columns = t.columns.map(col => ({ ...col, length: 0, nullable: !col.primaryKey }))
}

function handlePreviewDDL() {
  if (!tableForm.tableName) {
    ElMessage.warning('请输入表名')
    return
  }
  previewVisible.value = true
}

function handleExecute() {
  if (!tableForm.tableName) {
    ElMessage.warning('请输入表名')
    return
  }
  if (tableForm.columns.length === 0) {
    ElMessage.warning('请添加至少一个字段')
    return
  }
  
  executing.value = true
  setTimeout(() => {
    ElMessage.success('建表成功')
    executing.value = false
    previewVisible.value = false
    historyList.value.unshift({
      tableName: tableForm.tableName,
      time: new Date().toLocaleString(),
      status: '执行成功'
    })
    tableForm.tableName = ''
    tableForm.columns = []
  }, 2000)
}
</script>

<style lang="scss" scoped>
.table-create-container {
  .el-divider {
    margin: 20px 0;
  }
}
</style>

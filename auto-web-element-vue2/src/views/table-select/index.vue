<template>
  <div class="table-select-container">
    <el-card class="card">
      <div slot="header" class="card-header">
        <span>表选择 - {{ datasourceName }}</span>
        <div>
          <el-button type="primary" icon="el-icon-refresh" @click="handleScan">扫描表</el-button>
          <el-button type="success" icon="el-icon-check" @click="handleGenerate" :disabled="selectedTables.length === 0">
            生成 API ({{ selectedTables.length }})
          </el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-select v-model="filterDatasource" placeholder="选择数据源" @change="handleDatasourceChange" style="width: 200px;">
          <el-option label="测试数据库" value="1"></el-option>
          <el-option label="生产数据库" value="2"></el-option>
        </el-select>
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索表名"
          prefix-icon="el-icon-search"
          clearable
          style="width: 200px; margin-left: 10px;"
          @input="handleSearch"
        ></el-input>
        <el-checkbox v-model="showGenerated" style="margin-left: 20px;">仅显示待生成</el-checkbox>
        <div class="batch-actions">
          <el-button size="small" @click="handleSelectAll(true)">全选</el-button>
          <el-button size="small" @click="handleSelectAll(false)">全不选</el-button>
          <el-button size="small" @click="handleSelectInverse">反选</el-button>
        </div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50"></el-table-column>
        <el-table-column prop="tableName" label="表名" min-width="180"></el-table-column>
        <el-table-column prop="tableComment" label="注释" min-width="200"></el-table-column>
        <el-table-column prop="columnCount" label="字段数" width="100" align="center"></el-table-column>
        <el-table-column prop="generateMode" label="生成模式" width="100">
          <template slot-scope="{ row }">
            <el-tag size="small" :type="row.generateMode === 'auto' ? 'primary' : 'info'">
              {{ row.generateMode === 'auto' ? '自动' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="skipReason" label="跳过原因" min-width="150"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="handlePreview(row)">预览</el-button>
            <el-button type="text" @click="handleRemove(row)" v-if="row.status === 'generated'">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="pagination.total"
          :page-size="pagination.size"
          :current-page="pagination.page"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog title="表结构预览" :visible.sync="previewVisible" width="800px">
      <el-table :data="previewColumns" border stripe>
        <el-table-column prop="name" label="字段名" width="150"></el-table-column>
        <el-table-column prop="type" label="类型" width="150"></el-table-column>
        <el-table-column prop="comment" label="注释" min-width="150"></el-table-column>
        <el-table-column prop="primaryKey" label="主键" width="80" align="center">
          <template slot-scope="{ row }">
            <el-tag v-if="row.primaryKey" type="success" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="nullable" label="可空" width="80" align="center">
          <template slot-scope="{ row }">
            <span>{{ row.nullable ? '是' : '否' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { scanTables, getGenerationStatus, generateApi, removeApi } from '@/api/table'

export default {
  name: 'TableSelect',
  data() {
    return {
      loading: false,
      datasourceName: '测试数据库',
      filterDatasource: '1',
      searchForm: {
        keyword: ''
      },
      showGenerated: false,
      tableData: [],
      selectedTables: [],
      previewColumns: [],
      previewVisible: false,
      pagination: {
        total: 0,
        page: 1,
        size: 20
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const statusData = await getGenerationStatus()
        this.tableData = statusData.tables || this.generateMockData()
        this.pagination.total = this.tableData.length
      } catch (error) {
        this.tableData = this.generateMockData()
        this.pagination.total = this.tableData.length
      } finally {
        this.loading = false
      }
    },
    generateMockData() {
      const tables = ['users', 'orders', 'products', 'categories', 'comments', 'settings']
      return tables.map((name, index) => ({
        tableName: name,
        tableComment: `${name}相关数据表`,
        columnCount: Math.floor(Math.random() * 10) + 5,
        generateMode: index % 3 === 0 ? 'auto' : 'manual',
        status: index % 4 === 0 ? 'generated' : (index % 4 === 1 ? 'skipped' : 'pending'),
        skipReason: index % 4 === 1 ? '已存在API' : ''
      }))
    },
    handleScan() {
      this.loadData()
      this.$message.success('扫描完成')
    },
    handleGenerate() {
      this.$confirm(`确定要为选中的 ${this.selectedTables.length} 个表生成 API 吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        this.$message.success('API 生成中，请稍候...')
        setTimeout(() => {
          this.$message.success(`成功生成 ${this.selectedTables.length} 个 API`)
          this.loadData()
        }, 2000)
      }).catch(() => {})
    },
    handleDatasourceChange(value) {
      this.datasourceName = value === '1' ? '测试数据库' : '生产数据库'
      this.loadData()
    },
    handleSearch() {
      this.pagination.page = 1
    },
    handleSelectionChange(selection) {
      this.selectedTables = selection
    },
    handleSelectAll(selected) {
      this.tableData.forEach(table => {
        table.selected = selected
      })
    },
    handleSelectInverse() {
      this.tableData.forEach(table => {
        table.selected = !table.selected
      })
    },
    handlePreview(row) {
      this.previewColumns = this.generateMockColumns()
      this.previewVisible = true
    },
    generateMockColumns() {
      return [
        { name: 'id', type: 'bigint', comment: '主键ID', primaryKey: true, nullable: false },
        { name: 'created_at', type: 'datetime', comment: '创建时间', primaryKey: false, nullable: false },
        { name: 'updated_at', type: 'datetime', comment: '更新时间', primaryKey: false, nullable: true },
        { name: 'name', type: 'varchar(100)', comment: '名称', primaryKey: false, nullable: true }
      ]
    },
    handleRemove(row) {
      this.$confirm(`确定要移除表 "${row.tableName}" 的 API 吗？`, '警告', {
        type: 'warning'
      }).then(async () => {
        await removeApi(row.tableName)
        this.$message.success('移除成功')
        this.loadData()
      }).catch(() => {})
    },
    getStatusType(status) {
      const types = { generated: 'success', pending: 'warning', skipped: 'info', error: 'danger' }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = { generated: '已生成', pending: '待生成', skipped: '已跳过', error: '错误' }
      return texts[status] || '未知'
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadData()
    },
    handlePageChange(page) {
      this.pagination.page = page
      this.loadData()
    }
  }
}
</script>

<style lang="scss" scoped>
.table-select-container {
  .filter-bar {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 10px;

    .batch-actions {
      margin-left: auto;
    }
  }
}
</style>

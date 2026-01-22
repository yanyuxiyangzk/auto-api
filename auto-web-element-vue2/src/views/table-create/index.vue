<template>
  <div class="table-create-container">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card class="card">
          <div slot="header" class="card-header">
            <span>在线建表</span>
            <div>
              <el-button icon="el-icon-document" @click="handlePreviewDDL">预览 DDL</el-button>
              <el-button type="primary" icon="el-icon-check" @click="handleExecute" :loading="executing">
                执行建表
              </el-button>
            </div>
          </div>

          <el-form :model="tableForm" label-width="100px" size="medium">
            <el-form-item label="表名" required>
              <el-input v-model="tableForm.tableName" placeholder="请输入表名（小写字母、下划线）" clearable></el-input>
            </el-form-item>
            <el-form-item label="注释">
              <el-input v-model="tableForm.tableComment" placeholder="请输入表注释"></el-input>
            </el-form-item>

            <el-divider>字段定义</el-divider>

            <el-table :data="tableForm.columns" border stripe>
              <el-table-column label="字段名" width="150">
                <template slot-scope="{ row, $index }">
                  <el-input v-model="row.name" placeholder="字段名" size="small"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="类型" width="180">
                <template slot-scope="{ row }">
                  <el-select v-model="row.type" placeholder="选择类型" size="small" style="width: 100%;">
                    <el-option-group label="整数">
                      <el-option label="TINYINT" value="TINYINT"></el-option>
                      <el-option label="SMALLINT" value="SMALLINT"></el-option>
                      <el-option label="INT" value="INT"></el-option>
                      <el-option label="BIGINT" value="BIGINT"></el-option>
                    </el-option-group>
                    <el-option-group label="字符串">
                      <el-option label="VARCHAR(255)" value="VARCHAR(255)"></el-option>
                      <el-option label="TEXT" value="TEXT"></el-option>
                      <el-option label="LONGTEXT" value="LONGTEXT"></el-option>
                    </el-option-group>
                    <el-option-group label="日期时间">
                      <el-option label="DATE" value="DATE"></el-option>
                      <el-option label="DATETIME" value="DATETIME"></el-option>
                      <el-option label="TIMESTAMP" value="TIMESTAMP"></el-option>
                    </el-option-group>
                    <el-option-group label="其他">
                      <el-option label="DECIMAL(10,2)" value="DECIMAL(10,2)"></el-option>
                      <el-option label="BOOLEAN" value="BOOLEAN"></el-option>
                    </el-option-group>
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="长度" width="100">
                <template slot-scope="{ row }">
                  <el-input-number v-model="row.length" :min="0" size="small" style="width: 100%;"></el-input-number>
                </template>
              </el-table-column>
              <el-table-column label="注释">
                <template slot-scope="{ row }">
                  <el-input v-model="row.comment" placeholder="注释" size="small"></el-input>
                </template>
              </el-table-column>
              <el-table-column label="主键" width="70" align="center">
                <template slot-scope="{ row }">
                  <el-checkbox v-model="row.primaryKey"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column label="可空" width="70" align="center">
                <template slot-scope="{ row }">
                  <el-checkbox v-model="row.nullable"></el-checkbox>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template slot-scope="{ row, $index }">
                  <el-button type="text" icon="el-icon-delete" @click="handleDeleteColumn($index)" size="small"></el-button>
                </template>
              </el-table-column>
            </el-table>

            <div style="margin-top: 15px;">
              <el-button icon="el-icon-plus" @click="handleAddColumn">添加字段</el-button>
            </div>

            <el-divider>高级选项</el-divider>

            <el-form-item label="数据源">
              <el-select v-model="tableForm.datasourceId" placeholder="选择数据源" style="width: 300px;">
                <el-option label="测试数据库" value="1"></el-option>
                <el-option label="生产数据库" value="2"></el-option>
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
          <div slot="header" class="card-header">
            <span>模板</span>
          </div>
          <el-button
            v-for="template in templates"
            :key="template.name"
            type="text"
            @click="applyTemplate(template)"
            style="display: block; margin: 10px 0;"
          >
            <i :class="template.icon"></i> {{ template.name }}
          </el-button>
        </el-card>

        <el-card class="card" style="margin-top: 20px;">
          <div slot="header" class="card-header">
            <span>SQL 预览</span>
          </div>
          <pre class="code-block" v-text="previewSQL"></pre>
        </el-card>

        <el-card class="card" style="margin-top: 20px;">
          <div slot="header" class="card-header">
            <span>建表历史</span>
          </div>
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
    <el-dialog title="DDL 预览" :visible.sync="previewVisible" width="700px">
      <pre class="code-block" v-text="previewSQL"></pre>
      <span slot="footer" class="dialog-footer">
        <el-button @click="previewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExecute" :loading="executing">执行</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TableCreate',
  data() {
    return {
      executing: false,
      previewVisible: false,
      tableForm: {
        tableName: '',
        tableComment: '',
        datasourceId: '1',
        columns: [
          { name: 'id', type: 'BIGINT', length: 0, comment: '主键ID', primaryKey: true, nullable: false },
          { name: 'created_at', type: 'DATETIME', length: 0, comment: '创建时间', primaryKey: false, nullable: false },
          { name: 'updated_at', type: 'DATETIME', length: 0, comment: '更新时间', primaryKey: false, nullable: true }
        ],
        afterActions: []
      },
      templates: [
        { name: '用户表', icon: 'el-icon-user', template: { tableName: 'sys_user', tableComment: '用户表', columns: [
          { name: 'id', type: 'BIGINT', comment: '主键', primaryKey: true },
          { name: 'username', type: 'VARCHAR(50)', comment: '用户名', nullable: false },
          { name: 'password', type: 'VARCHAR(100)', comment: '密码' },
          { name: 'email', type: 'VARCHAR(100)', comment: '邮箱' },
          { name: 'status', type: 'INT', comment: '状态 0-禁用 1-启用' }
        ]}},
        { name: '订单表', icon: 'el-icon-tickets', template: { tableName: 'biz_order', tableComment: '订单表', columns: [
          { name: 'id', type: 'BIGINT', comment: '主键', primaryKey: true },
          { name: 'order_no', type: 'VARCHAR(50)', comment: '订单号', nullable: false },
          { name: 'amount', type: 'DECIMAL(10,2)', comment: '金额' },
          { name: 'status', type: 'INT', comment: '订单状态' }
        ]}},
        { name: '日志表', icon: 'el-icon-document', template: { tableName: 'sys_log', tableComment: '操作日志', columns: [
          { name: 'id', type: 'BIGINT', comment: '主键', primaryKey: true },
          { name: 'action', type: 'VARCHAR(50)', comment: '操作类型' },
          { name: 'content', type: 'TEXT', comment: '日志内容' },
          { name: 'operator', type: 'VARCHAR(50)', comment: '操作人' }
        ]}}
      ],
      historyList: [
        { tableName: 'test_table', time: '2024-01-20 10:30', status: '执行成功' },
        { tableName: 'user_profile', time: '2024-01-19 15:20', status: '执行成功' }
      ]
    }
  },
  computed: {
    previewSQL() {
      const { tableName, tableComment, columns } = this.tableForm
      if (!tableName) return '-- 请输入表名'
      
      let sql = `CREATE TABLE \`${tableName}\` (\n`
      const columnDefs = columns.map(col => {
        let definition = `  \`${col.name}\` ${col.type}`
        if (col.length > 0) definition += `(${col.length})`
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
    }
  },
  methods: {
    handleAddColumn() {
      this.tableForm.columns.push({
        name: '',
        type: 'VARCHAR(255)',
        length: 255,
        comment: '',
        primaryKey: false,
        nullable: true
      })
    },
    handleDeleteColumn(index) {
      this.tableForm.columns.splice(index, 1)
    },
    applyTemplate(template) {
      const t = template.template
      this.tableForm.tableName = t.tableName
      this.tableForm.tableComment = t.tableComment
      this.tableForm.columns = t.columns.map(col => ({
        ...col,
        length: 0,
        nullable: !col.primaryKey
      }))
    },
    handlePreviewDDL() {
      if (!this.tableForm.tableName) {
        this.$message.warning('请输入表名')
        return
      }
      this.previewVisible = true
    },
    handleExecute() {
      if (!this.tableForm.tableName) {
        this.$message.warning('请输入表名')
        return
      }
      if (this.tableForm.columns.length === 0) {
        this.$message.warning('请添加至少一个字段')
        return
      }
      
      this.executing = true
      setTimeout(() => {
        this.$message.success('建表成功')
        this.executing = false
        this.previewVisible = false
        this.historyList.unshift({
          tableName: this.tableForm.tableName,
          time: new Date().toLocaleString(),
          status: '执行成功'
        })
        this.tableForm.tableName = ''
        this.tableForm.columns = []
      }, 2000)
    }
  }
}
</script>

<style lang="scss" scoped>
.table-create-container {
  .el-divider {
    margin: 20px 0;
  }
}
</style>

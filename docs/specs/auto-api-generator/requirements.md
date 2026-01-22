# Auto-API-Generator 需求规格文档

## 1. 项目概述

### 1.1 简介
Auto-API-Generator 是一个基于 Spring Boot 2 的自动化 API 接口生成组件包，支持 REST 和 GraphQL 双协议。该组件能够根据数据库表结构自动生成对应的 API 接口，并提供在线 Groovy 脚本编写功能，支持动态热部署。组件设计为可复用的 Spring Boot Starter，可被微服务等外部系统便捷引入并调用。

### 1.2 目标
- 减少手动编写 API 接口的工作量
- 支持本地数据库表和第三方数据源
- 提供 REST 和 GraphQL 两种接口类型
- 通过在线 Groovy 脚本支持复杂业务逻辑定制
- 具备良好的可复用性和扩展性

---

## 2. 用户故事

### 2.1 作为后端开发人员，我希望能根据数据库表自动生成 API 接口，这样我就不用手动编写重复的 CRUD 代码。

### 2.2 作为微服务架构师，我希望这个功能能以组件包的形式被引入，这样可以在多个项目中复用同一套 API 生成逻辑。

### 2.3 作为前端开发人员，我希望能够使用 GraphQL 接口来灵活获取数据，这样我可以精确控制需要返回的字段。

### 2.4 作为业务分析师，我希望能够在线编写 Groovy 脚本来实现复杂业务逻辑，这样就不用每次都找开发人员修改代码。

### 2.5 作为运维人员，我希望 Groovy 脚本能够动态热部署，这样修改后可以立即生效而无需重启服务。

### 2.6 作为项目负责人，我希望支持多种数据库类型，这样可以在不同技术栈的项目中使用。

---

## 3. 功能需求

### 3.1 API 自动生成

**3.1.1 表选择机制**

| 编号 | 需求描述 |
|------|----------|
| REQ-F0-001 | 系统应支持**全量自动选择**模式，扫描数据源下所有表并生成 API |
| REQ-F0-002 | 系统应支持**手动选择**模式，用户可勾选需要生成 API 的表 |
| REQ-F0-003 | 系统应提供**黑白名单**机制，支持正则表达式过滤表名 |
| REQ-F0-004 | 系统应检测并**跳过已存在 API 的表**，避免路由冲突 |
| REQ-F0-005 | 前端应提供直观的表选择界面，显示每个表的生成状态（已生成/待生成/已跳过） |
| REQ-F0-006 | 系统应支持批量操作（全选/全不选/反选） |

**3.1.2 热部署机制**

| 编号 | 需求描述 |
|------|----------|
| REQ-F0-007 | 系统应支持**数据源配置热更新**，新增/修改数据源后无需重启服务即可连接 |
| REQ-F0-008 | 系统应支持**表选择热更新**，修改表选择后新增/移除的 API 立即生效 |
| REQ-F0-009 | 系统应支持**API 规则热更新**，修改生成规则后自动刷新路由 |
| REQ-F0-010 | 系统应提供**刷新 API 配置**的 API 接口或管理界面操作 |
| REQ-F0-011 | 热部署过程中应保证**原子性**，配置错误时回滚到上一版本 |
| REQ-F0-012 | 热部署操作应记录操作日志，便于审计和回滚 |

**3.1.3 REST 接口生成**

| 编号 | 需求描述 |
|------|----------|
| REQ-F1-001 | 系统应支持根据数据库表自动生成 RESTful API 接口 |
| REQ-F1-002 | 自动生成的接口应包括标准的 CRUD 操作（Create、Read、Update、Delete） |
| REQ-F1-003 | 自动生成的接口应支持分页查询功能 |
| REQ-F1-004 | 自动生成的接口应支持条件查询（支持多字段组合查询） |
| REQ-F1-005 | 自动生成的接口应支持排序功能 |
| REQ-F1-006 | 系统应支持自定义 API 路径和请求方法 |
| REQ-F1-007 | 自动生成的接口应返回统一的响应格式 |

**3.1.2 GraphQL 接口生成**

| 编号 | 需求描述 |
|------|----------|
| REQ-F2-001 | 系统应支持根据数据库表自动生成 GraphQL Schema |
| REQ-F2-002 | 自动生成的 GraphQL 接口应支持查询（Query）操作 |
| REQ-F2-003 | 自动生成的 GraphQL 接口应支持变更（Mutation）操作 |
| REQ-F2-004 | 系统应支持自动生成关联表查询（连表查询） |
| REQ-F2-005 | 系统应支持 GraphQL 聚合查询（count、sum、avg 等） |
| REQ-F2-006 | 自动生成的 GraphQL 接口应支持分页（Relay Connection 规范） |

### 3.2 数据源管理

**3.2.1 本地数据源**

| 编号 | 需求描述 |
|------|----------|
| REQ-F3-001 | 系统应支持连接本地 MySQL 数据库 |
| REQ-F3-002 | 系统应支持连接本地 PostgreSQL 数据库 |
| REQ-F3-003 | 系统应支持读取本地数据库的表结构信息（元数据） |
| REQ-F3-004 | 系统应自动识别表的主键、字段类型、注释等信息 |

**3.2.2 第三方数据源**

| 编号 | 需求描述 |
|------|----------|
| REQ-F4-001 | 系统应支持配置第三方数据库连接信息 |
| REQ-F4-002 | 系统应支持动态添加/删除第三方数据源 |
| REQ-F4-003 | 系统应支持跨数据源查询（Federation） |
| REQ-F4-004 | 第三方数据源的连接信息应加密存储 |
| REQ-F4-005 | 系统应支持连接池管理，保证连接稳定性 |

### 3.3 Groovy 脚本管理

**3.3.1 脚本编辑**

| 编号 | 需求描述 |
|------|----------|
| REQ-F5-001 | 系统应提供在线代码编辑器支持 Groovy 脚本编写 |
| REQ-F5-002 | 编辑器应支持语法高亮功能 |
| REQ-F5-003 | 编辑器应支持代码自动补全功能 |
| REQ-F5-004 | 编辑器应支持语法错误检测 |
| REQ-F5-005 | 系统应提供脚本模板库，方便快速创建常用脚本 |

**3.3.2 脚本执行**

| 编号 | 需求描述 |
|------|----------|
| REQ-F6-001 | 系统应支持 Groovy 脚本作为 API 拦截器（前置/后置处理） |
| REQ-F6-002 | 系统应支持 Groovy 脚本完全替换自动生成的 API 逻辑 |
| REQ-F6-003 | 系统应支持 Groovy 脚本定义自定义路由和逻辑 |
| REQ-F6-004 | Groovy 脚本应能访问数据库上下文（执行查询/更新） |
| REQ-F6-005 | Groovy 脚本应能获取请求参数和响应对象 |
| REQ-F6-006 | 系统应支持 Groovy 脚本返回自定义数据结构 |

**3.3.3 动态热部署**

| 编号 | 需求描述 |
|------|----------|
| REQ-F7-001 | 系统应支持脚本修改后立即生效（无需重启服务） |
| REQ-F7-002 | 系统应提供脚本版本管理，支持回滚到历史版本 |
| REQ-F7-003 | 系统应记录脚本执行日志 |
| REQ-F7-004 | 系统应提供脚本执行异常处理机制 |

**3.3.4 安全控制**

| 编号 | 需求描述 |
|------|----------|
| REQ-F8-001 | 系统应对 Groovy 脚本启用沙箱限制，禁止危险操作 |
| REQ-F8-002 | 应禁止脚本执行文件 IO 操作 |
| REQ-F8-003 | 应禁止脚本执行网络访问操作 |
| REQ-F8-004 | 应禁止脚本执行系统命令操作 |
| REQ-F8-005 | 系统应支持配置脚本执行超时时间 |
| REQ-F8-006 | 系统应支持脚本审核机制（可选） |

### 3.4 前端功能（Vue2 & Vue3）

**3.4.1 通用功能**

| 编号 | 需求描述 |
|------|----------|
| REQ-F9-001 | 前端应提供数据源管理界面（连接配置、测试连接） |
| REQ-F9-002 | 前端应提供表结构浏览界面 |
| REQ-F9-003 | 前端应提供 API 文档查看界面 |
| REQ-F9-004 | 前端应提供 API 测试工具（类似 Postman） |
| REQ-F9-005 | 前端应支持导出 API 文档（OpenAPI/Swagger 格式） |
| REQ-F9-006 | 前端应支持多语言（中文/英文） |

**3.4.2 脚本管理界面**

| 编号 | 需求描述 |
|------|----------|
| REQ-F10-001 | 前端应提供脚本列表管理界面 |
| REQ-F10-002 | 前端应提供脚本在线编辑界面 |
| REQ-F10-003 | 前端应提供脚本版本历史查看界面 |
| REQ-F10-004 | 前端应提供脚本导入导出功能 |
| REQ-F10-005 | 前端应提供脚本调试界面（执行测试） |

### 3.5 组件包特性

**3.5.1 可复用性**

| 编号 | 需求描述 |
|------|----------|
| REQ-F11-001 | 系统应打包为 Spring Boot Starter 形式 |
| REQ-F11-002 | 引入方应只需简单配置即可使用 |
| REQ-F11-003 | 组件应支持与引入方现有数据源集成 |
| REQ-F11-004 | 组件应支持自定义配置（端口、路径前缀等） |
| REQ-F11-005 | 组件应提供 SPI 机制支持扩展 |

**3.5.2 兼容性**

| 编号 | 需求描述 |
|------|----------|
| REQ-F12-001 | 组件应兼容 Spring Boot 2.x 版本 |
| REQ-F12-002 | 组件应兼容 Java 8 及以上版本 |
| REQ-F12-003 | 组件应与主流 ORM 框架兼容（MyBatis-Plus、JPA） |
| REQ-F12-004 | 组件应支持与引入方的认证授权系统集成 |

---

## 4. 非功能需求

### 4.1 性能需求

| 编号 | 需求描述 |
|------|----------|
| REQ-NF1-001 | API 接口响应时间应 < 500ms（不含业务逻辑） |
| REQ-NF1-002 | 应支持至少 100 个并发 API 请求 |
| REQ-NF1-003 | Groovy 脚本首次执行应 < 2s，后续执行应 < 100ms |
| REQ-NF1-004 | 应支持缓存机制，减少数据库元数据查询 |

### 4.2 安全需求

| 编号 | 需求描述 |
|------|----------|
| REQ-NF2-001 | 第三方数据源密码应加密存储 |
| REQ-NF2-002 | API 接口应支持 XSS 防护 |
| REQ-NF2-003 | API 接口应支持 SQL 注入防护 |
| REQ-NF2-004 | 应支持与引入方的安全框架集成（Spring Security 等） |

### 4.3 可用性需求

| 编号 | 需求描述 |
|------|----------|
| REQ-NF3-001 | 组件应提供详细的使用文档 |
| REQ-NF3-002 | 组件应提供快速开始示例 |
| REQ-NF3-003 | 应提供健康检查接口 |

### 4.4 可维护性需求

| 编号 | 需求描述 |
|------|----------|
| REQ-NF4-001 | 代码应遵循阿里巴巴开发规约 |
| REQ-NF4-002 | 应提供完整的单元测试（覆盖率 > 60%） |
| REQ-NF4-003 | 代码应有清晰的注释 |

---

## 5. 接口定义

### 5.1 REST API 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/tables | 获取数据表列表 |
| GET | /api/tables/{tableName} | 获取表结构详情 |
| GET | /api/tables/{tableName}/data | 获取表数据（分页、筛选） |
| POST | /api/tables/{tableName}/data | 新增数据 |
| PUT | /api/tables/{tableName}/data/{id} | 更新数据 |
| DELETE | /api/tables/{tableName}/data/{id} | 删除数据 |
| POST | /api/scripts | 创建脚本 |
| PUT | /api/scripts/{id} | 更新脚本 |
| DELETE | /api/scripts/{id} | 删除脚本 |
| POST | /api/scripts/{id}/execute | 执行脚本 |
| GET | /api/datasources | 获取数据源列表 |
| POST | /api/datasources | 添加数据源 |
| PUT | /api/datasources/{id} | 更新数据源 |
| DELETE     | /api/datasources/{id} | 删除数据源 |
| POST | /api/tables/generate | 根据选择的表生成 API |
| POST | /api/tables/refresh | 热刷新 API 配置（重新生成路由） |
| POST | /api/tables/{tableName}/remove | 移除指定表的 API |
| GET | /api/tables/status | 获取所有表的 API 生成状态 |

### 5.2 GraphQL 接口

| 类型 | 操作 | 描述 |
|------|------|------|
| Query | tables | 查询表列表 |
| Query | table | 查询表结构 |
| Query | tableData | 查询表数据 |
| Mutation | createTableData | 新增数据 |
| Mutation | updateTableData | 更新数据 |
| Mutation | deleteTableData | 删除数据 |
| Query | scripts | 查询脚本列表 |
| Mutation | saveScript | 保存脚本 |
| Mutation | deleteScript | 删除脚本 |

---

## 6. 数据模型

### 6.1 数据源配置（DataSourceConfig）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long | 主键 |
| name | String | 数据源名称 |
| type | String | 数据源类型（mysql、postgresql 等） |
| host | String | 主机地址 |
| port | Integer | 端口号 |
| database | String | 数据库名 |
| username | String | 用户名 |
| password | String | 密码（加密） |
| status | Integer | 状态（0-禁用，1-启用） |

### 6.2 脚本（Script）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long | 主键 |
| name | String | 脚本名称 |
| type | String | 脚本类型（interceptor、override、custom） |
| targetTable | String | 目标表名（可选） |
| targetApi | String | 目标 API（可选） |
| content | String | Groovy 脚本内容 |
| version | Integer | 版本号 |
| status | Integer | 状态（0-草稿，1-发布） |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

### 6.3 脚本版本（ScriptVersion）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long | 主键 |
| scriptId | Long | 脚本 ID |
| content | String | 脚本内容 |
| version | Integer | 版本号 |
| createdAt | DateTime | 创建时间 |
| createdBy | String | 创建人 |

### 6.4 表选择配置（TableSelection）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long | 主键 |
| datasourceId | Long | 数据源 ID |
| tableName | String | 表名 |
| selected | Boolean | 是否选中生成 API |
| generateMode | String | 生成模式（auto/manual） |
| apiPrefix | String | API 路径前缀（可选自定义） |
| skipReason | String | 跳过原因（如已存在 API、正则过滤等） |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

### 6.5 API 生成状态（ApiGenerationStatus）

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long | 主键 |
| datasourceId | Long | 数据源 ID |
| tableName | String | 表名 |
| status | String | 状态（generated/pending/skipped/error） |
| restApiPath | String | REST API 路径 |
| graphqlType | String | GraphQL Type 名称 |
| generatedAt | DateTime | 生成时间 |
| errorMessage | String | 错误信息（如果生成失败） |

---

## 7. 项目目录结构

```
auto-api-generator/
├── auto-api-core/                    # 核心模块
│   ├── src/main/java/...
│   │   └── com/iflow/api/core/
│   │       ├── config/               # 配置类
│   │       ├── controller/           # 控制器
│   │       ├── service/              # 服务层
│   │       ├── repository/           # 数据访问层
│   │       ├── entity/               # 实体类
│   │       ├── dto/                  # 数据传输对象
│   │       ├── graphql/              # GraphQL 相关
│   │       ├── groovy/               # Groovy 脚本引擎
│   │       ├── datasource/           # 多数据源管理
│   │       └── util/                 # 工具类
│   └── src/main/resources/
│
├── auto-api-starter/                 # Spring Boot Starter
│   ├── pom.xml
│   └── src/main/java/...
│       └── com/iflow/api/starter/
│           └── AutoApiAutoConfiguration.java
│
├── auto-web-vue2/                    # Vue2 前端
│   ├── public/
│   ├── src/
│   │   ├── api/                      # API 接口
│   │   ├── components/               # 组件
│   │   ├── views/                    # 页面
│   │   ├── store/                    # Vuex
│   │   └── router/                   # 路由
│   └── package.json
│
├── auto-web-vue3/                    # Vue3 前端
│   ├── public/
│   ├── src/
│   │   ├── api/                      # API 接口
│   │   ├── components/               # 组件
│   │   ├── views/                    # 页面
│   │   ├── stores/                   # Pinia
│   │   └── router/                   # 路由
│   └── package.json
│
└── docs/
    └── specs/                        # 规格文档
```

---

## 8. 技术选型

| 类别 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.x |
| ORM 框架 | MyBatis-Plus | 3.5.x |
| GraphQL | graphql-java | 20.x |
| 脚本引擎 | Groovy | 3.0.x |
| 数据库 | MySQL / PostgreSQL | 5.7+ / 10+ |
| 前端框架 | Vue 2 / Vue 3 | 2.7.x / 3.3.x |
| UI 库 | Element UI / Element Plus | 2.x / 2.x |
| 构建工具 | Maven / Vite | 3.x / 5.x |

---

文档版本：v1.0
创建日期：2026-01-21

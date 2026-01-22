# Auto-API-Generator 实现任务清单

## 任务概述

本文档将设计文档转换为可执行的代码实现任务清单，按模块和优先级组织。

---

## 1. 项目基础架构

### 1.1 创建项目结构和配置文件
- [x] **1.1.1** 创建 Maven 多模块项目结构
  - 创建 `pom.xml` 父项目配置
  - 创建 `auto-api-core` 模块
  - 创建 `auto-api-starter` 模块
  - 配置依赖版本管理
  - 引用：REQ-NF4-001（阿里巴巴开发规约）

### 1.2 创建基础配置类
- [x] **1.2.1** 创建 Spring Boot 自动配置类
  - 创建 `AutoApiAutoConfiguration.java`
  - 配置条件注解（@ConditionalOnClass）
  - 配置属性绑定（@EnableConfigurationProperties）
  - 引用：REQ-F11-001（打包为 Spring Boot Starter）

---

## 2. 核心实体与数据访问层

### 2.1 实体类定义
- [x] **2.1.1** 创建数据源配置实体
  - 创建 `DatasourceConfig.java`
  - 实现 JPA 注解映射
  - 引用：REQ-F4-001（配置第三方数据库连接）

- [x] **2.1.2** 创建表选择配置实体
  - 创建 `TableSelection.java`
  - 实现选择状态映射
  - 引用：REQ-F0-001（全量自动选择）、REQ-F0-002（手动选择）

- [x] **2.1.3** 创建 API 生成状态实体
  - 创建 `ApiGenerationStatus.java`
  - 实现状态追踪
  - 引用：REQ-F0-010（刷新 API 配置接口）

- [ ] **2.1.4** 创建 Groovy 脚本实体
  - 创建 `Script.java`
  - 创建 `ScriptVersion.java`
  - 引用：REQ-F5-001（在线代码编辑器）、REQ-F7-002（版本管理）

### 2.2 Repository 层实现
- [x] **2.2.1** 创建数据源配置 Repository
  - 创建 `DatasourceConfigRepository.java`
  - 实现连接测试查询方法
  - 引用：REQ-F4-002（动态添加/删除数据源）

- [x] **2.2.2** 创建表选择配置 Repository
  - 创建 `TableSelectionRepository.java`
  - 实现按数据源查询方法
  - 引用：REQ-F0-003（黑白名单机制）

- [ ] **2.2.3** 创建脚本 Repository
  - 创建 `ScriptRepository.java`
  - 创建 `ScriptVersionRepository.java`
  - 引用：REQ-F10-001（脚本列表管理）

---

## 3. DTO 与工具类

### 3.1 DTO 定义
- [x] **3.1.1** 创建元数据 DTO
  - 创建 `TableMeta.java`（表元数据）
  - 创建 `ColumnMeta.java`（字段元数据）
  - 创建 `IndexMeta.java`（索引元数据）
  - 引用：REQ-F3-003（读取表结构信息）

- [x] **3.1.2** 创建前端展示 DTO
  - 创建 `TableInfo.java`（表信息）
  - 创建 `TableScanResult.java`（扫描结果）
  - 引用：REQ-F9-002（表结构浏览界面）

- [ ] **3.1.3** 创建脚本上下文 DTO
  - 创建 `ScriptContext.java`（脚本执行上下文）
  - 引用：REQ-F6-004（访问数据库上下文）、REQ-F6-005（获取请求参数）

### 3.2 工具类实现
- [x] **3.2.1** 创建类型转换器
  - 创建 `TypeConverter.java`
  - 实现数据库类型到 Java 类型的映射
  - 引用：REQ-F3-004（识别主键、字段类型）

- [x] **3.2.2** 创建命名转换器
  - 创建 `NamingConverter.java`
  - 实现下划线命名与驼峰命名互转
  - 引用：设计文档-组件设计（命名转换）

- [ ] **3.2.3** 创建统一响应 DTO
  - 创建 `ApiResponse.java`
  - 实现成功/失败响应封装
  - 引用：REQ-F1-007（统一响应格式）

---

## 4. 数据源管理模块

### 4.1 动态数据源管理
- [ ] **4.1.1** 创建动态数据源路由
  - 创建 `DynamicDataSource.java`
  - 继承 AbstractRoutingDataSource
  - 实现数据源上下文切换
  - 引用：REQ-F4-003（跨数据源查询）

- [ ] **4.1.2** 创建数据源注册表
  - 创建 `DataSourceRegistry.java`
  - 管理多个数据源连接池
  - 引用：REQ-F4-005（连接池管理）

- [ ] **4.1.3** 创建数据源上下文持有者
  - 创建 `DataSourceHolder.java`
  - 实现数据源切换的线程隔离
  - 引用：设计文档-数据源管理

### 4.2 数据源管理服务
- [ ] **4.2.1** 创建数据源服务
  - 创建 `DatasourceService.java`
  - 实现 CRUD 操作
  - 实现连接测试方法
  - 引用：REQ-F4-001（配置第三方数据库）、REQ-F4-004（加密存储）

---

## 5. 元数据服务模块

### 5.1 元数据解析
- [ ] **5.1.1** 创建元数据服务
  - 创建 `MetadataService.java`
  - 实现 JDBC MetaData API 调用
  - 引用：REQ-F3-003（读取表结构信息）

- [ ] **5.1.2** 实现表结构解析
  - 实现 `getTables()` 方法
  - 实现 `getColumns()` 方法
  - 实现 `getPrimaryKeys()` 方法
  - 引用：REQ-F3-004（自动识别主键、字段类型）

---

## 6. 表选择服务模块

### 6.1 表扫描与选择
- [ ] **6.1.1** 创建表选择服务
  - 创建 `TableSelectionService.java`
  - 实现扫描数据源所有表
  - 实现冲突检测（已存在 API 跳过）
  - 引用：REQ-F0-004（检测并跳过已存在 API）

- [ ] **6.1.2** 实现黑白名单过滤
  - 实现正则表达式匹配
  - 实现跳过原因标记
  - 引用：REQ-F0-003（黑白名单机制）

- [ ] **6.1.3** 实现批量选择配置
  - 实现全选/全不选/反选
  - 实现配置持久化
  - 引用：REQ-F0-006（批量操作）

---

## 7. 动态路由注册模块

### 7.1 动态控制器
- [ ] **7.1.1** 创建通用动态控制器
  - 创建 `DynamicController.java`
  - 实现通用 CRUD 操作
  - 引用：REQ-F1-002（标准 CRUD 操作）

- [ ] **7.1.2** 实现分页查询
  - 实现 `list()` 方法
  - 支持分页参数
  - 支持筛选条件
  - 引用：REQ-F1-003（分页查询功能）

- [ ] **7.1.3** 实现条件查询
  - 实现 `query()` 方法
  - 支持多字段组合查询
  - 引用：REQ-F1-004（条件查询）

- [ ] **7.1.4** 实现排序功能
  - 实现 `orderBy` 参数处理
  - 支持升序/降序
  - 引用：REQ-F1-005（排序功能）

### 7.2 路由注册器
- [ ] **7.2.1** 创建动态路由注册器
  - 创建 `DynamicRouteRegistry.java`
  - 实现 ApplicationContextAware
  - 实现 Bean 动态注册
  - 引用：REQ-F0-007（数据源配置热更新）

- [ ] **7.2.2** 实现 REST 路由注册
  - 使用 RequestMappingHandlerMapping
  - 注册 GET/POST/PUT/DELETE 路由
  - 引用：REQ-F1-001（生成 RESTful API）

- [ ] **7.2.3** 实现热刷新机制
  - 实现 `hotRefresh()` 方法
  - 实现 `registerApi()` 方法
  - 实现 `unregisterApi()` 方法
  - 引用：REQ-F0-008（表选择热更新）、REQ-F0-009（API 规则热更新）

---

## 8. GraphQL 模块

### 8.1 Schema 生成器
- [ ] **8.1.1** 创建 GraphQL Schema 生成器
  - 创建 `GraphQLSchemaGenerator.java`
  - 实现根据表结构生成 Type
  - 引用：REQ-F2-001（生成 GraphQL Schema）

- [ ] **8.1.2** 实现类型映射
  - 实现 Java 类型到 GraphQL 类型映射
  - 引用：REQ-F2-002（查询操作）

### 8.2 Resolver 实现
- [ ] **8.2.1** 创建 DataFetcher 注册表
  - 创建 `DataFetcherRegistry.java`
  - 实现查询/变更的 DataFetcher
  - 引用：REQ-F2-003（变更操作）

- [ ] **8.2.2** 实现分页连接类型
  - 实现 Relay Connection 规范
  - 实现 PageInfo
  - 引用：REQ-F2-006（分页规范）

- [ ] **8.2.3** 创建 GraphQL 配置类
  - 创建 `GraphQLConfig.java`
  - 配置 GraphQL Servlet
  - 引用：设计文档-组件设计（GraphQL 配置）

---

## 9. 通用查询服务模块

### 9.1 动态 SQL 构建
- [ ] **9.1.1** 创建 SQL 生成器
  - 创建 `SqlGenerator.java`
  - 实现动态 SQL 构建
  - 引用：REQ-F1-003~005（查询功能）

- [ ] **9.1.2** 创建通用查询服务
  - 创建 `GenericQueryService.java`
  - 实现分页查询
  - 实现 CRUD 操作
  - 引用：REQ-F1-002（标准 CRUD）

---

## 10. 热部署模块

### 10.1 热部署服务
- [ ] **10.1.1** 创建热部署服务
  - 创建 `HotDeployService.java`
  - 实现 API 生成入口
  - 引用：REQ-F0-007~009（热部署机制）

- [ ] **10.1.2** 创建配置刷新监听器
  - 创建 `ConfigChangeEventListener.java`
  - 监听配置变更事件
  - 引用：REQ-F0-012（操作日志记录）

### 10.2 状态追踪
- [ ] **10.2.1** 实现生成状态更新
  - 创建 `ApiGenerationStatusRepository.java`
  - 实现状态记录和查询
  - 引用：REQ-F0-005（显示生成状态）

---

## 11. 在线建表模块

### 11.1 建表服务
- [ ] **11.1.1** 创建在线建表服务
  - 创建 `TableCreateService.java`
  - 实现 DDL 生成
  - 实现 DDL 执行
  - 引用：需求-在线建表功能

- [ ] **11.1.2** 创建建表请求 DTO
  - 创建 `CreateTableRequest.java`
  - 创建 `ColumnDefinition.java`
  - 引用：需求-表单填写建表

- [ ] **11.1.3** 创建建表控制器
  - 创建 `TableCreateController.java`
  - 实现预览和执行接口
  - 引用：需求-手动确认执行

---

## 12. Groovy 脚本模块

### 12.1 脚本引擎
- [ ] **12.1.1** 创建 Groovy 沙箱
  - 创建 `GroovySandbox.java`
  - 实现静态安全检查
  - 实现 SecureClassLoader
  - 引用：REQ-F8-001~004（安全控制）

- [ ] **12.1.2** 创建脚本执行器
  - 创建 `GroovyScriptExecutor.java`
  - 实现脚本编译和执行
  - 引用：REQ-F6-001~006（脚本执行）

- [ ] **12.1.3** 创建脚本服务
  - 创建 `GroovyScriptService.java`
  - 实现版本管理
  - 实现执行日志
  - 引用：REQ-F7-002（版本管理）、REQ-F7-003（执行日志）

### 12.2 脚本安全配置
- [ ] **12.2.1** 创建 Groovy 配置类
  - 创建 `GroovyConfig.java`
  - 配置脚本引擎
  - 配置沙箱
  - 引用：REQ-F8-005（超时配置）

---

## 13. REST Controller 层

### 13.1 数据源控制器
- [ ] **13.1.1** 创建数据源控制器
  - 创建 `DatasourceController.java`
  - 实现 CRUD 接口
  - 实现连接测试
  - 引用：REQ-F9-001（数据源管理界面）

### 13.2 表管理控制器
- [ ] **13.2.1** 创建表控制器
  - 创建 `TableController.java`
  - 实现扫描接口
  - 实现生成接口
  - 实现刷新接口
  - 引用：REQ-F0-010（刷新接口）、REQ-F9-002（表结构浏览）

### 13.3 脚本控制器
- [ ] **13.3.1** 创建脚本控制器
  - 创建 `ScriptController.java`
  - 实现 CRUD 接口
  - 实现执行接口
  - 引用：REQ-F10-001~005（脚本管理界面）

---

## 14. 单元测试

### 14.1 Service 层测试
- [ ] **14.1.1** 测试表选择服务
  - 创建 `TableSelectionServiceTest.java`
  - 测试冲突检测
  - 引用：REQ-F0-004（跳过已存在 API）

- [ ] **14.1.2** 测试动态路由注册
  - 创建 `DynamicRouteRegistryTest.java`
  - 测试路由注册
  - 测试热刷新
  - 引用：REQ-F0-007~009（热部署）

- [ ] **14.1.3** 测试 Groovy 沙箱
  - 创建 `GroovySandboxTest.java`
  - 测试危险操作拦截
  - 引用：REQ-F8-001~004（安全控制）

### 14.2 Repository 层测试
- [ ] **14.2.1** 测试数据源配置仓储
  - 创建 `DatasourceConfigRepositoryTest.java`
  - 引用：REQ-NF4-002（单元测试覆盖率 > 60%）

### 14.3 Controller 层测试
- [ ] **14.3.1** 测试 REST API
  - 创建 `ControllerIntegrationTest.java`
  - 使用 MockMvc 测试
  - 引用：REQ-F1-001~007（REST 接口）

---

## 15. Vue2 前端

### 15.1 项目基础
- [ ] **15.1.1** 初始化 Vue2 项目
  - 创建 `auto-web-vue2` 目录结构
  - 配置 Element UI
  - 配置路由
  - 引用：REQ-F9-001~006（前端功能）

### 15.2 通用组件
- [ ] **15.2.1** 创建 API 响应封装
  - 创建 `api.js` 封装
  - 引用：设计文档-前端架构

- [ ] **15.2.2** 创建表单组件
  - 创建 `TableSelection.vue` 组件
  - 引用：REQ-F0-005（表选择界面）

### 15.3 页面实现
- [ ] **15.3.1** 数据源管理页面
  - 创建 `views/datasource/index.vue`
  - 引用：REQ-F9-001（数据源管理界面）

- [ ] **15.3.2** 表选择页面
  - 创建 `views/table-select/index.vue`
  - 引用：REQ-F0-005（表选择界面）

- [ ] **15.3.3** 在线建表页面
  - 创建 `views/table-create/index.vue`
  - 引用：需求-在线建表

- [ ] **15.3.4** 脚本编辑页面
  - 创建 `views/script-editor/index.vue`
  - 引用：REQ-F10-002（脚本在线编辑）

- [ ] **15.3.5** API 文档页面
  - 创建 `views/api-docs/index.vue`
  - 引用：REQ-F9-003（API 文档界面）

---

## 16. Vue3 前端

### 16.1 项目基础
- [ ] **16.1.1** 初始化 Vue3 项目
  - 创建 `auto-web-vue3` 目录结构
  - 配置 Element Plus
  - 配置 Pinia 状态管理
  - 引用：REQ-F9-001~006（前端功能）

### 16.2 组件与页面
- [ ] **16.2.1** 复用 Vue2 组件逻辑
  - 适配 Composition API
  - 引用：设计文档-前端架构

- [ ] **16.2.2** 实现相同功能页面
  - 数据源管理页面
  - 表选择页面
  - 在线建表页面
  - 脚本编辑页面
  - API 文档页面
  - 引用：REQ-F9-001~006（前端功能）

---

## 17. 配置文件

### 17.1 Spring Boot 配置
- [ ] **17.1.1** 创建核心模块配置
  - 创建 `auto-api-core/src/main/resources/application.yml`
  - 引用：REQ-F11-004（自定义配置）

- [ ] **17.1.2** 创建 Starter 配置属性类
  - 创建 `AutoApiProperties.java`
  - 引用：REQ-F11-002（简单配置即可使用）

---

## 18. 集成测试

### 18.1 API 端到端测试
- [ ] **18.1.1** 测试 REST API 完整流程
  - 创建 `RestApiE2ETest.java`
  - 测试 CRUD 操作
  - 引用：REQ-F1-002（标准 CRUD）

- [ ] **18.1.2** 测试 GraphQL API
  - 创建 `GraphQLApiE2ETest.java`
  - 测试查询和变更
  - 引用：REQ-F2-002~003（查询/变更）

### 18.2 热部署测试
- [ ] **18.2.1** 测试热刷新功能
  - 创建 `HotDeployE2ETest.java`
  - 测试配置变更后路由更新
  - 引用：REQ-F0-007~009（热部署）

---

## 任务执行顺序

建议按以下顺序执行任务：

1. **阶段一**：项目基础（1.1 ~ 1.2）
2. **阶段二**：实体与数据访问（2.1 ~ 2.2）
3. **阶段三**：工具类与 DTO（3.1 ~ 3.2）
4. **阶段四**：数据源管理（4.1 ~ 4.2）
5. **阶段五**：元数据服务（5.1）
6. **阶段六**：表选择服务（6.1）
7. **阶段七**：动态路由注册（7.1 ~ 7.2）
8. **阶段八**：GraphQL 模块（8.1 ~ 8.2）
9. **阶段九**：通用查询服务（9.1）
10. **阶段十**：热部署模块（10.1 ~ 10.2）
11. **阶段十一**：在线建表（11.1）
12. **阶段十二**：Groovy 脚本（12.1 ~ 12.2）
13. **阶段十三**：Controller 层（13.1 ~ 13.3）
14. **阶段十四**：单元测试（14.1 ~ 14.3）
15. **阶段十五**：Vue2 前端（15.1 ~ 15.3）
16. **阶段十六**：Vue3 前端（16.1 ~ 16.2）
17. **阶段十七**：配置文件（17.1）
18. **阶段十八**：集成测试（18.1 ~ 18.2）

---

文档版本：v1.0
创建日期：2026-01-21

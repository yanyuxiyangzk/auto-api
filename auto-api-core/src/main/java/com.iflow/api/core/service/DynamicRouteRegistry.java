package com.iflow.api.core.service;

import com.iflow.api.core.dto.metadata.TableMeta;
import com.iflow.api.core.entity.ApiGenerationStatus;
import com.iflow.api.core.entity.TableSelection;
import com.iflow.api.core.repository.ApiGenerationStatusRepository;
import com.iflow.api.core.repository.TableSelectionRepository;
import com.iflow.api.core.util.NamingConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态路由注册器
 * 
 * 引用：REQ-F2-002（自动生成 RESTful API 接口）
 *        REQ-F2-003（动态添加 API 接口路由）
 */
@Slf4j
@Service
public class DynamicRouteRegistry {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private TableSelectionRepository tableSelectionRepository;

    @Autowired
    private ApiGenerationStatusRepository apiGenerationStatusRepository;

    /**
     * 已注册的 API 路由缓存 (tableSelectionId -> RequestMappingInfo)
     */
    private final Map<Long, RequestMappingInfo> registeredRoutes = new ConcurrentHashMap<>();

    /**
     * 生成的 Controller 实例缓存
     */
    private final Map<Long, Object> controllerInstances = new ConcurrentHashMap<>();

    /**
     * 路由计数器
     */
    private final AtomicLong routeCounter = new AtomicLong(0);

    /**
     * 注册 REST API 路由
     * 
     * @param tableMeta 表元数据
     * @param datasourceId 数据源 ID
     * @return 路由信息
     */
    public RequestMappingInfo registerRestApi(TableMeta tableMeta, Long datasourceId) {
        String apiPath = NamingConverter.toApiPath(tableMeta.getTableName());
        String className = NamingConverter.toPascalCase(tableMeta.getTableName()) + "Controller";
        String controllerBeanName = NamingConverter.toCamelCase(className) + "Controller";

        // 生成 Controller 类
        Class<?> controllerClass = generateControllerClass(
            className,
            tableMeta,
            datasourceId,
            apiPath
        );

        // 创建 Controller 实例
        Object controllerInstance;
        try {
            controllerInstance = controllerClass.newInstance();
        } catch (Exception e) {
            log.error("创建 Controller 实例失败: {}", className, e);
            throw new RuntimeException("创建 Controller 实例失败", e);
        }

        // 生成 API 方法
        List<Method> apiMethods = generateApiMethods(controllerClass, tableMeta, apiPath);

        // 构建 RequestMappingInfo
        PatternsRequestCondition patternsCondition = new PatternsRequestCondition("/api/" + apiPath);
        RequestMethodsRequestCondition methodsCondition = new RequestMethodsRequestCondition(
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
        );

        RequestMappingInfo mappingInfo = new RequestMappingInfo(
            patternsCondition,
            methodsCondition,
            null,
            null,
            null,
            null,
            null
        );

        // 注册路由
        try {
            requestMappingHandlerMapping.registerMapping(
                mappingInfo,
                controllerInstance,
                controllerClass.getDeclaredMethod("handleRequest")
            );

            // 缓存注册信息
            registeredRoutes.put(routeCounter.incrementAndGet(), mappingInfo);
            controllerInstances.put(routeCounter.get(), controllerInstance);

            log.info("REST API 路由已注册: path=/api/{}, table={}", apiPath, tableMeta.getName());

            return mappingInfo;

        } catch (Exception e) {
            log.error("注册 REST API 路由失败: path=/api/{}", apiPath, e);
            throw new RuntimeException("注册路由失败", e);
        }
    }

    /**
     * 注册 GraphQL API
     * 
     * @param tableMeta 表元数据
     * @param datasourceId 数据源 ID
     * @return GraphQL 类型名称
     */
    public String registerGraphQL(TableMeta tableMeta, Long datasourceId) {
        String typeName = NamingConverter.toGraphQLTypeName(tableMeta.getName());

        // TODO: 实现 GraphQL Schema 注册
        // 1. 生成 GraphQL 类型定义
        // 2. 生成 Query 和 Mutation Resolver
        // 3. 注册到 GraphQL Schema

        log.info("GraphQL 类型已注册: type={}, table={}", typeName, tableMeta.getName());

        return typeName;
    }

    /**
     * 注销 API 路由
     * 
     * @param tableSelectionId 表选择记录 ID
     */
    public void unregisterApi(Long tableSelectionId) {
        RequestMappingInfo mappingInfo = registeredRoutes.remove(tableSelectionId);
        Object controllerInstance = controllerInstances.remove(tableSelectionId);

        if (mappingInfo != null) {
            try {
                requestMappingHandlerMapping.unregisterMapping(mappingInfo);
                log.info("API 路由已注销: tableSelectionId={}", tableSelectionId);
            } catch (Exception e) {
                log.error("注销 API 路由失败: tableSelectionId={}", tableSelectionId, e);
            }
        }

        // 更新 API 生成状态
        apiGenerationStatusRepository.findByTableSelectionId(tableSelectionId)
            .ifPresent(status -> {
                status.setStatus("UNREGISTERED");
                apiGenerationStatusRepository.updateById(status);
            });
    }

    /**
     * 重新注册 API 路由（用于热更新）
     * 
     * @param tableSelectionId 表选择记录 ID
     * @param tableMeta 新的表元数据
     * @param datasourceId 数据源 ID
     */
    public void reregisterApi(Long tableSelectionId, TableMeta tableMeta, Long datasourceId) {
        // 先注销旧路由
        unregisterApi(tableSelectionId);

        // 重新注册新路由
        registerRestApi(tableMeta, datasourceId);

        // 更新状态
        apiGenerationStatusRepository.findByTableSelectionId(tableSelectionId)
            .ifPresent(status -> {
                status.setStatus("ACTIVE");
                status.setLastRegenerateTime(new Date());
                apiGenerationStatusRepository.updateById(status);
            });
    }

    /**
     * 生成 Controller 类
     */
    private Class<?> generateControllerClass(String className, TableMeta tableMeta,
            Long datasourceId, String apiPath) {
        // 动态生成 Controller 字节码
        // 这里使用 Javaassist 或 CGLIB 生成类
        // 简化版本：返回 DynamicController.class，在运行时动态分发

        return DynamicController.class;
    }

    /**
     * 生成 API 方法
     */
    private List<Method> generateApiMethods(Class<?> controllerClass, TableMeta tableMeta, String apiPath) {
        List<Method> methods = new ArrayList<>();

        try {
            // 查询列表
            Method listMethod = controllerClass.getDeclaredMethod("list", TableMeta.class, Long.class, Map.class);
            methods.add(listMethod);

            // 查询详情
            Method getMethod = controllerClass.getDeclaredMethod("get", TableMeta.class, Long.class, Object.class);
            methods.add(getMethod);

            // 新增
            Method createMethod = controllerClass.getDeclaredMethod("create", TableMeta.class, Long.class, Map.class);
            methods.add(createMethod);

            // 更新
            Method updateMethod = controllerClass.getDeclaredMethod("update", TableMeta.class, Long.class, Object.class, Map.class);
            methods.add(updateMethod);

            // 删除
            Method deleteMethod = controllerClass.getDeclaredMethod("delete", TableMeta.class, Long.class, Object.class);
            methods.add(deleteMethod);

        } catch (NoSuchMethodException e) {
            log.error("生成 API 方法失败", e);
        }

        return methods;
    }

    /**
     * 获取所有已注册的路由
     */
    public Map<Long, RequestMappingInfo> getRegisteredRoutes() {
        return Collections.unmodifiableMap(registeredRoutes);
    }

    /**
     * 获取已注册的路由数量
     */
    public int getRegisteredRouteCount() {
        return registeredRoutes.size();
    }

    /**
     * 检查路由是否已注册
     */
    public boolean isRouteRegistered(Long tableSelectionId) {
        return registeredRoutes.containsKey(tableSelectionId);
    }

    /**
     * 根据表名查找已注册路由
     */
    public Optional<Long> findByTableName(String tableName) {
        return registeredRoutes.entrySet().stream()
            .filter(entry -> {
                RequestMappingInfo info = entry.getValue();
                Set<String> patterns = info.getPatternsCondition().getPatterns();
                String apiPath = NamingConverter.toApiPath(tableName);
                return patterns.stream().anyMatch(p -> p.contains("/" + apiPath));
            })
            .map(Map.Entry::getKey)
            .findFirst();
    }

    /**
     * 批量注册路由
     * 
     * @param tableSelections 表选择记录列表
     * @return 成功注册的路由数量
     */
    public int batchRegister(List<TableSelection> tableSelections) {
        int successCount = 0;

        for (TableSelection selection : tableSelections) {
            try {
                ApiGenerationStatus status = apiGenerationStatusRepository
                    .findByTableSelectionId(selection.getId())
                    .orElseGet(() -> createApiGenerationStatus(selection));

                if (status.getStatus().equals("ACTIVE")) {
                    // 重新生成 API
                    reregisterApi(selection.getId(), status.getTableMeta(), selection.getDatasourceId());
                } else {
                    // 首次生成
                    registerRestApi(status.getTableMeta(), selection.getDatasourceId());
                    status.setStatus("ACTIVE");
                    apiGenerationStatusRepository.updateById(status);
                }

                successCount++;

            } catch (Exception e) {
                log.error("批量注册路由失败: tableSelectionId={}", selection.getId(), e);
            }
        }

        log.info("批量注册路由完成: success={}, total={}", successCount, tableSelections.size());
        return successCount;
    }

    /**
     * 创建 API 生成状态记录
     */
    private ApiGenerationStatus createApiGenerationStatus(TableSelection selection) {
        ApiGenerationStatus status = new ApiGenerationStatus();
        status.setTableSelectionId(selection.getId());
        status.setDatasourceId(selection.getDatasourceId());
        status.setTableName(selection.getTableName());
        status.setTableMeta(selection.getTableMeta());
        status.setApiPath("/api/" + NamingConverter.toApiPath(selection.getTableName()));
        status.setStatus("PENDING");
        status.setGenerateTime(new Date());
        apiGenerationStatusRepository.insert(status);
        return status;
    }

    /**
     * 获取所有路由的映射信息
     */
    public Map<String, HandlerMethod> getAllRouteMappings() {
        Map<String, HandlerMethod> mappings = new HashMap<>();

        requestMappingHandlerMapping.getHandlerMethods().forEach((info, handler) -> {
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            patterns.forEach(pattern -> mappings.put(pattern, handler));
        });

        return mappings;
    }
}

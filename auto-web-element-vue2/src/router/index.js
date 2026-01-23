import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hiddenLayout: true }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { hiddenLayout: true }
  }
]

export const asyncRoutes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '仪表盘', icon: 'dashboard' }
  },
  {
    path: '/datasource',
    name: 'Datasource',
    component: () => import('@/views/datasource/index.vue'),
    meta: { title: '数据源管理', icon: 'database' }
  },
  {
    path: '/table-select',
    name: 'TableSelect',
    component: () => import('@/views/table-select/index.vue'),
    meta: { title: '表选择', icon: 'table' }
  },
  {
    path: '/table-create',
    name: 'TableCreate',
    component: () => import('@/views/table-create/index.vue'),
    meta: { title: '在线建表', icon: 'edit' }
  },
  {
    path: '/script-editor',
    name: 'ScriptEditor',
    component: () => import('@/views/script-editor/index.vue'),
    meta: { title: '脚本编辑', icon: 'code' }
  },
  {
    path: '/api-docs',
    name: 'ApiDocs',
    component: () => import('@/views/api-docs/index.vue'),
    meta: { title: 'API文档', icon: 'documentation' }
  },
  {
    path: '/api-test',
    name: 'ApiTest',
    component: () => import('@/views/api-test/index.vue'),
    meta: { title: 'API测试', icon: 'component' }
  },
  {
    path: '*',
    redirect: '/404',
    meta: { hiddenLayout: true }
  }
]

const routes = [...constantRoutes, ...asyncRoutes]

const router = new VueRouter({
  routes
})

export default router

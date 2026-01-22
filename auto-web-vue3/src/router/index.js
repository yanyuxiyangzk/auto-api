import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/components/common/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'datasource',
        name: 'Datasource',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源管理' }
      },
      {
        path: 'table-select',
        name: 'TableSelect',
        component: () => import('@/views/table-select/index.vue'),
        meta: { title: '表选择' }
      },
      {
        path: 'table-create',
        name: 'TableCreate',
        component: () => import('@/views/table-create/index.vue'),
        meta: { title: '在线建表' }
      },
      {
        path: 'script-editor',
        name: 'ScriptEditor',
        component: () => import('@/views/script-editor/index.vue'),
        meta: { title: '脚本编辑' }
      },
      {
        path: 'api-docs',
        name: 'ApiDocs',
        component: () => import('@/views/api-docs/index.vue'),
        meta: { title: 'API 文档' }
      },
      {
        path: 'api-test',
        name: 'ApiTest',
        component: () => import('@/views/api-test/index.vue'),
        meta: { title: 'API 测试' }
      }
    ]
  },
  {
    path: '/redirect/:path(.*)',
    name: 'Redirect',
    component: () => import('@/views/redirect/index.vue'),
    meta: { title: '重定向', public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - Auto API Generator` : 'Auto API Generator'
  next()
})

export default router

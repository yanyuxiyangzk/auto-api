import { asyncRoutes, constantRoutes } from '@/router'

const state = {
  routes: [],
  addRoutes: [],
  defaultRoutes: [],
  topbarRoutes: []
}

const mutations = {
  SET_ROUTES: (state, routes) => {
    state.addRoutes = routes
    state.routes = constantRoutes.concat(routes)
  },
  SET_DEFAULT_ROUTES: (state, routes) => {
    state.defaultRoutes = constantRoutes.concat(routes)
  },
  SET_TOPBAR_ROUTES: (state, routes) => {
    state.topbarRoutes = routes
  }
}

const actions = {
  generateRoutes({ commit }) {
    return new Promise(resolve => {
      const accessedRoutes = filterAsyncRoutes(asyncRoutes)
      commit('SET_ROUTES', accessedRoutes)
      resolve(accessedRoutes)
    })
  }
}

// 过滤异步路由
function filterAsyncRoutes(routes) {
  const res = []
  routes.forEach(route => {
    const tmp = { ...route }
    if (tmp.children) {
      tmp.children = filterAsyncRoutes(tmp.children)
    }
    if (!tmp.hidden) {
      res.push(tmp)
    }
  })
  return res
}

// getters
const getters = {
  permission_routes: state => state.routes
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

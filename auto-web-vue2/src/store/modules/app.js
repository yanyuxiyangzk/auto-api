const state = {
  sidebarCollapsed: false,
  language: 'zh-CN',
  theme: 'light'
}

const mutations = {
  TOGGLE_SIDEBAR(state) {
    state.sidebarCollapsed = !state.sidebarCollapsed
  },
  SET_LANGUAGE(state, language) {
    state.language = language
  },
  SET_THEME(state, theme) {
    state.theme = theme
  }
}

const actions = {
  toggleSidebar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  },
  setLanguage({ commit }, language) {
    commit('SET_LANGUAGE', language)
  },
  setTheme({ commit }, theme) {
    commit('SET_THEME', theme)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

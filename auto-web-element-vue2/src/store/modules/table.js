const state = {
  tables: [],
  selections: [],
  generationStatus: {},
  loading: false
}

const mutations = {
  SET_TABLES(state, tables) {
    state.tables = tables
  },
  SET_SELECTIONS(state, selections) {
    state.selections = selections
  },
  UPDATE_SELECTION(state, { tableName, selected }) {
    const table = state.tables.find(t => t.tableName === tableName)
    if (table) {
      table.selected = selected
    }
  },
  SET_GENERATION_STATUS(state, status) {
    state.generationStatus = status
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  }
}

const actions = {
  setTables({ commit }, tables) {
    commit('SET_TABLES', tables)
  },
  setSelections({ commit }, selections) {
    commit('SET_SELECTIONS', selections)
  },
  updateSelection({ commit }, payload) {
    commit('UPDATE_SELECTION', payload)
  },
  setGenerationStatus({ commit }, status) {
    commit('SET_GENERATION_STATUS', status)
  },
  setLoading({ commit }, loading) {
    commit('SET_LOADING', loading)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

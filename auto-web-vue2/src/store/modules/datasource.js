const state = {
  list: [],
  current: null,
  loading: false
}

const mutations = {
  SET_LIST(state, list) {
    state.list = list
  },
  SET_CURRENT(state, current) {
    state.current = current
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  ADD(state, item) {
    state.list.push(item)
  },
  UPDATE(state, item) {
    const index = state.list.findIndex(i => i.id === item.id)
    if (index !== -1) {
      state.list.splice(index, 1, item)
    }
  },
  DELETE(state, id) {
    const index = state.list.findIndex(i => i.id === id)
    if (index !== -1) {
      state.list.splice(index, 1)
    }
  }
}

const actions = {
  setList({ commit }, list) {
    commit('SET_LIST', list)
  },
  setCurrent({ commit }, current) {
    commit('SET_CURRENT', current)
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

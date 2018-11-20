import * as api from '@/api'

const state = () => {
  return {
    filter: '',
    loading: false,
    response: []
  }
}

const getters = {}

const actions = {
  async getTimeline ({commit, state, rootState}) {
    const {datasource, engine} = rootState.hash
    const {filter} = state

    commit('setLoading', {data: true})

    try {
      const data = await api.getTimeline(datasource, engine, filter)
      if (data.comments) {
        commit('setResponse', {data: data.comments})
      }
      commit('setLoading', {data: true})
      return data
    } catch (e) {
      commit('setLoading', {data: true})
    }
  }
}

const mutations = {
  init (state) {
    state.filter = ''
    state.response = []
  },
  setFilter (state, {data}) {
    state.filter = data
  },
  setLoading (state, {data}) {
    state.loading = data
  },
  setResponse (state, {data}) {
    state.response = data
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

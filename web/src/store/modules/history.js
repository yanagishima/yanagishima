import * as api from '@/api'
import * as ls from '@/store/localStorage'

const LIMIT_INITIAL = 100
const LIMIT_MORE = 1024

function lsKey (datasource) {
  return `histories_${datasource}`
}

const state = () => {
  return {
    historyIds: [],
    filter: '',
    limit: LIMIT_INITIAL,
    loading: false,
    response: null,

    panelQueryid: null,
    panelResult: null,
    label: null
  }
}

const getters = {
  hasMore (state) {
    return !!state.response && state.response.hit > state.limit
  }
}

const actions = {
  async getHistories ({commit, state, rootState}, {isMore}) {
    const {datasource, engine} = rootState.hash
    const {isLocalStorage} = rootState.settings

    isMore = isMore || false

    if (isLocalStorage && state.historyIds.length === 0) {
      commit('setResponse', {data: null})
      return false
    }

    if (!isMore) {
      commit('setLoading', {data: true})
    }

    let data
    try {
      if (isLocalStorage) {
        data = await api.queryHistoryById(datasource, state.historyIds, state.label || '')
      } else {
        commit('setLimit', {data: isMore ? LIMIT_MORE : LIMIT_INITIAL})
        data = await api.queryHistoryUser(datasource, engine, state.filter, 0, state.limit, state.label)
      }
    } catch (e) {
      commit('setLoading', {data: false})
      throw e
    }

    let results = data.results
    if (results) {
      results = results.sortBy(r => r[0], true)
    }

    commit('setResponse', {data: Object.assign({}, data, {results})})
    commit('setLoading', {data: false})
  },
  async loadPanelQuery ({commit, rootState}, {queryid}) {
    const {datasource, engine} = rootState.hash
    if (!queryid) {
      return false
    }
    commit('setPanelQueryid', {data: queryid})
    commit('setPanelResult', {data: null})
    const data = await api.getQueryResult(datasource, engine, queryid)
    if (!Object.isEmpty(data)) {
      commit('setPanelResult', {data})
    }
  },
  async importHistory ({commit, dispatch, rootState}, {data}) {
    const {datasource} = rootState.hash
    commit('overwriteLocalStorage', {datasource})
    dispatch('getHistories', {isMore: false})
  },
  async deleteAllLocalHistory ({commit, dispatch, rootState}) {
    const {datasource} = rootState.hash
    commit('deleteLocalStorage', {datasource})
    dispatch('getHistories', {isMore: false})
  }
}

const mutations = {
  init (state) {
    state.filter = ''
    state.limit = LIMIT_INITIAL
    state.response = null
    state.panelQueryid = null
    state.panelResult = null
    state.label = null
  },
  setHistoryId (state, {datasource, historyId}) {
    state.historyIds = state.historyIds.add(historyId, 0).unique()
    ls.setItem(lsKey(datasource), state.historyIds)
  },
  setFilter (state, {data}) {
    state.filter = data
  },
  setLimit (state, {data}) {
    state.limit = data
  },
  setLoading (state, {data}) {
    state.loading = data
  },
  setResponse (state, {data}) {
    state.response = data
  },
  setPanelQueryid (state, {data}) {
    state.panelQueryid = data
  },
  setPanelResult (state, {data}) {
    state.panelResult = data
  },
  loadLocalStorage (state, {datasource, max}) {
    max = max || LIMIT_MORE
    state.historyIds = ls.getItemArray(lsKey(datasource), max)
  },
  overwriteLocalStorage (state, {datasource, historyStr}) {
    state.historyIds = historyStr.split(',')
    ls.setItem(lsKey(datasource), state.historyIds)
  },
  deleteLocalStorage (state, {datasource}) {
    state.historyIds = []
    ls.setItem(lsKey(datasource), [])
  },
  setLabel (state, {data}) {
    state.label = data
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

import * as api from '@/api'
import * as ls from '@/store/localStorage'

let timer

const state = () => {
  return {
    now: '',
    isOpenQuery: false,
    isAdminMode: false,
    loading: false,
    response: [],
    error: null
  }
}

const getters = {}

const actions = {
  async getQlist ({commit, rootState, rootGetters}, {isAutoQlist}) {
    const {datasource} = rootState.hash
    const {isPresto, isHive, isSpark, isElasticsearch, authInfo} = rootGetters

    isAutoQlist = isAutoQlist || false

    commit('refreshNow')

    if (!isAutoQlist) {
      commit('setLoading', {data: true})
    }

    try {
      let data
      if (isPresto) {
        data = await api.getQlistPresto(datasource, authInfo)
      } else if (isHive) {
        data = await api.getQlistHive(datasource)
      } else if (isSpark) {
        data = await api.getQlistSpark(datasource)
      } else if (isElasticsearch) {
        commit('setLoading', {data: false})
        return
      } else {
        throw new Error('not supported')
      }
      if (data.error) {
        commit('setError', {data: data.error})
      } else {
        commit('setResponse', {data})
      }
    } catch (e) {
    }

    commit('setLoading', {data: false})
  },
  async autoQlist ({dispatch, rootState, rootGetters}, {enable}) {
    const {isPresto} = rootGetters
    const refreshPeriod = 1
    const period = isPresto ? 1000 : 1000 * 10

    enable = enable || false

    clearInterval(timer)
    dispatch('getQlist', {isAutoQlist: false})

    let time = refreshPeriod
    if (enable) {
      timer = setInterval(() => {
        const {isModal} = rootState
        const {tab} = rootState.hash

        time--
        if (time === 0) {
          if (tab === 'qlist' && !isModal) {
            dispatch('getQlist', {isAutoQlist: true})
          }
          time = refreshPeriod
        }
      }, period)
    }
  }
}

const mutations = {
  init (state) {
    state.response = []
    state.error = null
  },
  refreshNow (state) {
    state.now = Date.create().format('{yyyy}/{M}/{d} {24hr}:{mm}:{ss}')
  },
  setLoading (state, {data}) {
    state.loading = data
  },
  setResponse (state, {data}) {
    state.response = data
  },
  setError (state, {data}) {
    state.error = data
  },
  setIsOpenQuery (state, {data}) {
    state.isOpenQuery = data
    ls.setItemBoolean('openQuery', state.isOpenQuery)
  },
  setIsAdminMode (state, {data}) {
    state.isAdminMode = data
    ls.setItemBoolean('adminMode', state.isAdminMode)
  },
  loadLocalStorage (state) {
    state.isOpenQuery = ls.getItemBoolean('openQuery', false)
    state.isAdminMode = ls.getItemBoolean('adminMode', false)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

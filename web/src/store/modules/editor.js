import * as api from '@/api'
import {COMPLETE_LIST} from '@/constants'

const state = () => {
  return {
    inputQuery: '',
    gotoLine: 0,
    focus: 1, // watchで変更を検知してフォーカスするため、booleanでなくnumber
    errorLine: -1,
    errorText: '',
    completeWords: []
  }
}

const getters = {}

const actions = {
  async formatQuery ({commit, state}) {
    const query = state.inputQuery
    if (query) {
      const data = await api.formatQuery(query)
      if (data.formattedQuery) {
        commit('setInputQuery', {data: data.formattedQuery})
      } else if (data.error) {
        commit('setError', {text: data.error, line: data.errorLineNumber - 1})
      }
    }
  },
  async validateQuery ({commit, state, rootState}) {
    const {datasource} = rootState.hash
    const query = state.inputQuery
    const data = await api.validateQuery(datasource, query)
    if (data.error) {
      commit('setError', {text: data.error, line: data.errorLineNumber ? data.errorLineNumber - 1 : 0})
    } else {
      commit('setError', {text: '', line: -1})
    }
  },
  async convertQuery ({commit, state, rootGetters}) {
    const {isPresto, isHive} = rootGetters
    const query = state.inputQuery
    if (query) {
      if (!isPresto && !isHive) {
        throw new Error('not supported')
      }

      let data
      if (isPresto) {
        data = await api.convertQueryToHive(query)
      } else if (isHive) {
        data = await api.convertQueryToPresto(query)
      }
      let convertedQuery
      if (isPresto) {
        convertedQuery = data.hiveQuery
      } else if (isHive) {
        convertedQuery = data.prestoQuery
      }
      if (convertedQuery) {
        commit('setInputQuery', {data: convertedQuery})
      } else if (data.error) {
        commit('setError', {text: data.error, line: -1})
      }
    }
  },
  async getCompleteWords ({commit, rootState, rootGetters}) {
    const {isPresto} = rootGetters

    if (!isPresto) {
      return false
    }

    const completeList = Object.assign({}, {table: []}, COMPLETE_LIST)
    const completeWords = []
    for (const [key, words] of Object.entries(completeList)) {
      for (let word of words) {
        if (key === 'snippet') {
          word = word.format({yesterday: Date.create().addDays(-1).format('{yyyy}{MM}{dd}')})
        }
        completeWords.push({
          caption: word.truncate(70, 'left'),
          value: word,
          meta: key
        })
      }
    }
    commit('setCompleteWords', {data: completeWords})
  }
}

const mutations = {
  init (state) {
    state.inputQuery = ''
  },
  setInputQuery (state, {data}) {
    state.inputQuery = data
  },
  setGotoLine (state, {line}) {
    state.gotoLine = line
  },
  setError (state, {line, text}) {
    state.errorLine = line
    state.errorText = text
  },
  resetError (state) {
    state.errorLine = -1
    state.errorText = ''
  },
  focusOnEditor (state) {
    state.focus++
  },
  resetFocusOnEditor (state) {
    state.focus = 0
  },
  setCompleteWords (state, {data}) {
    state.completeWords = data
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

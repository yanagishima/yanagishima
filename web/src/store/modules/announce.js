import announce from '@/assets/data/announce.js'
import * as ls from '@/store/localStorage'

const state = () => {
  return {
    announce,
    announceReadId: -1
  }
}

const getters = {
  isUnread (state) {
    return state.announce && state.announce.id > state.announceReadId
  }
}

const actions = {}

const mutations = {
  read (state) {
    if (!state.announce) {
      return
    }
    const id = state.announce.id
    if (id > state.announceReadId) {
      state.announceReadId = id
      ls.setItem('announceReadId', id)
    }
  },
  loadLocalStorage (state) {
    state.announceReadId = ls.getItemNumber('announceReadId', -1)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

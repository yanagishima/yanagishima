import notifications from '@/assets/data/notifications.js'
import * as ls from '@/store/localStorage'

const state = () => {
  return {
    notifications,
    notificationReadId: -1
  }
}

const getters = {
  hasUnread (state) {
    return state.notifications.length > 0 && state.notifications[0].id > state.notificationReadId
  }
}

const actions = {}

const mutations = {
  read (state) {
    if (state.notifications.length === 0) {
      return
    }
    const latest = state.notifications[0].id
    if (latest > state.notificationReadId) {
      state.notificationReadId = latest
      ls.setItem('notificationReadId', latest)
    }
  },
  loadLocalStorage (state) {
    state.notificationReadId = ls.getItemNumber('notificationReadId', -1)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

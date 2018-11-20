import * as api from '@/api'
import * as ls from '@/store/localStorage'

function lsKey (datasource) {
  return `bookmarks_${datasource}`
}

const state = () => {
  return {
    bookmarks: [],
    loading: false,
    response: [],
    addedBookmarkId: null
  }
}

const getters = {}

const actions = {
  async getBookmarks ({state, commit, rootState}) {
    const {isLocalStorage} = rootState.settings
    const {datasource, engine} = rootState.hash

    if (isLocalStorage && state.bookmarks.length === 0) {
      commit('setResponse', {data: []})
      return false
    }

    if (!(datasource && engine)) {
      return false
    }

    commit('setLoading', {data: true})
    try {
      let data
      if (isLocalStorage) {
        data = await api.getBookmarksById(datasource, engine, state.bookmarks)
      } else {
        data = await api.getBookmarksUser(datasource, engine)
      }

      if (data.bookmarkList) {
        const list = data.bookmarkList
          .filter(b => b.engine === engine)
          .sortBy(b => b.bookmark_id, true)
        commit('setResponse', {data: list})
      }
    } catch (e) {
    }
    commit('setLoading', {data: false})
  },
  async getBookmark ({state, commit, rootState}, {bookmarkId}) {
    const {datasource, engine} = rootState.hash
    const data = await api.getBookmarksById(datasource, engine, [bookmarkId])
    if (data.bookmarkList && data.bookmarkList.length) {
      commit('editor/setInputQuery', {data: data.bookmarkList[0].query}, {root: true})
    }
  },
  async addBookmarkItem ({state, commit, dispatch, rootState}) {
    const {datasource, engine} = rootState.hash
    const query = rootState.editor.inputQuery
    const defaultTitle = Date.create().format('{yyyy}/{MM}/{dd} {24hr}:{mm}:{ss}')
    const title = prompt('Input bookmark title.', defaultTitle)
    if (title === null) {
      return false
    }

    try {
      const data = await api.addBookmarkItem(datasource, engine, title || defaultTitle, query)
      const bookmarkId = data.bookmark_id

      commit('setBookmarkItem', {datasource, bookmarkId})
      dispatch('getBookmarks')
      commit('result/reset', {}, {root: true})
      commit('setHashItem', {queryid: '', bookmark_id: bookmarkId, tab: 'bookmark'}, {root: true})
    } catch (e) {
    }
  },
  async deleteBookmarkItem ({state, commit, dispatch, rootState}, {bookmarkId}) {
    const {datasource, engine} = rootState.hash
    commit('deleteBookmark', {datasource, bookmarkId})
    await api.deleteBookmarkItem(datasource, engine, bookmarkId)
    dispatch('getBookmarks')
  },
  async importBookmarks ({commit, dispatch, rootState}, {data}) {
    const {datasource} = rootState.hash
    commit('overwriteLocalStorage', {datasource, bookmarksStr: data})
    await dispatch('getBookmarks')
  },
  async deleteAllLocalBookmarks ({commit, dispatch, rootState}) {
    const {datasource} = rootState.hash
    commit('deleteLocalStorage', {datasource})
    await dispatch('getBookmarks')
  }
}

const mutations = {
  init (state) {
    state.response = []
    state.addedBookmarkId = null
  },
  setBookmarkItem (state, {datasource, bookmarkId}) {
    state.addedBookmarkId = bookmarkId
    state.bookmarks = state.bookmarks.add(bookmarkId, 0).unique()
    ls.setItem(lsKey(datasource), state.bookmarks)
  },
  deleteBookmark (state, {datasource, bookmarkId}) {
    state.bookmarks.remove(bookmarkId)
    ls.setItem(lsKey(datasource), state.bookmarks)
  },
  setLoading (state, {data}) {
    state.loading = data
  },
  setResponse (state, {data}) {
    state.response = data
  },
  loadLocalStorage (state, {datasource, max}) {
    max = max || 100
    state.bookmarks = ls.getItemArrayNumber(lsKey(datasource), max)
  },
  overwriteLocalStorage (state, {datasource, bookmarksStr}) {
    state.bookmarks = bookmarksStr.split(',').map(Number)
    ls.setItem(lsKey(datasource), state.bookmarks)
  },
  deleteLocalStorage (state, {datasource}) {
    state.bookmarks = []
    ls.setItem(lsKey(datasource), [])
  },
  resetAddedBookmarkId (state) {
    state.addedBookmarkId = null
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

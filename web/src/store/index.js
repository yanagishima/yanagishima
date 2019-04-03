import Vue from 'vue'
import Vuex from 'vuex'
import toastr from 'toastr'
import {HASH_KEYS, TABS} from '@/constants'
import qlist from '@/store/modules/qlist'
import history from '@/store/modules/history'
import bookmark from '@/store/modules/bookmark'
import result from '@/store/modules/result'
import treeview from '@/store/modules/treeview'
import timeline from '@/store/modules/timeline'
import editor from '@/store/modules/editor'
import * as api from '@/api'
import * as ls from '@/store/localStorage'

Vue.use(Vuex)

const settings = {
  isWide: {
    default: false,
    lsKey: 'wide',
    type: 'boolean'
  },
  isAutoQlist: {
    default: false,
    lsKey: 'autoQlist',
    type: 'boolean'
  },
  desktopNotification: {
    default: false,
    lsKey: 'desktopNotification',
    type: 'boolean'
  },
  rememberDatasource: {
    default: false,
    lsKey: 'rememberDatasource',
    type: 'boolean'
  },
  rememberEngine: {
    default: false,
    lsKey: 'rememberEngine',
    type: 'boolean'
  },
  minline: {
    default: 4,
    lsKey: 'minline',
    type: 'number'
  },
  theme: {
    default: 'chrome',
    lsKey: 'theme',
    type: 'string'
  },
  isCsv: {
    default: false,
    lsKey: 'isCsv',
    type: 'boolean'
  },
  includeHeader: {
    default: true,
    lsKey: 'includeHeader',
    type: 'boolean'
  },
  isLocalStorage: {
    default: true,
    lsKey: 'localstorage',
    type: 'boolean'
  }
}

function initialHash () {
  return {
    datasource: '',
    engine: '',
    tab: '',
    queryid: '',
    bookmark_id: '',
    chart: 0,
    pivot: 0,
    line: 0,
    table: ''
  }
}

export default new Vuex.Store({
  strict: process.env.NODE_ENV !== 'production',
  modules: {
    qlist,
    history,
    bookmark,
    result,
    treeview,
    timeline,
    editor
  },
  state: {
    settings: Object.entries(settings).reduce((obj, [key, val]) => Object.assign(obj, {[key]: val.default}), {}),
    datasources: [],
    engines: {},
    auths: {},
    metadataServices: {},
    hash: initialHash(),
    authUser: null,
    authPass: null,
    isModal: false,
    isSettingOpen: false,
    isSideHistoryOpen: false,
    isSuperAdminMode: false,
    unload: true
  },
  getters: {
    hashString (state) {
      const hashObj = HASH_KEYS
        .filter(k => state.hash[k[0]] || k[1])
        .reduce((obj, k) => Object.assign(obj, {[k[0]]: state.hash[k[0]]}), {})
      return Object.toQueryString(hashObj, {deep: true})
    },
    isPresto (state) {
      return state.hash.engine === 'presto'
    },
    isHive (state) {
      return state.hash.engine === 'hive'
    },
    isSpark (state) {
      return state.hash.engine === 'spark'
    },
    isElasticsearch (state) {
      return state.hash.engine === 'elasticsearch'
    },
    authInfo (state) {
      return state.auths[state.hash.datasource] ? {
        user: state.authUser,
        password: state.authPass
      } : {}
    },
    authUserInfo (state) {
      return state.auths[state.hash.datasource] ? {
        user: state.authUser
      } : {}
    },
    datasourceIndex (state) {
      const i = state.datasources.indexOf(state.hash.datasource)
      return i === -1 ? 0 : i + 1
    },
    datasourceEngine (state) {
      return state.hash.datasource && state.hash.engine ? `${state.hash.datasource}_${state.hash.engine}` : ''
    },
    isBottomPanelOpen (state) {
      return state.isSideHistoryOpen && state.history.panelQueryid
    },
    isMetadataService (state) {
      return state.metadataServices[state.hash.datasource]
    }
  },
  mutations: {
    init (state) {
      state.hash = Object.assign({}, state.hash, {
        queryid: '',
        tab: TABS[0].id,
        line: 0,
        chart: 0,
        pivot: 0,
        bookmark_id: ''
      })
    },
    setAuth (state, {user, pass}) {
      const {datasource} = state.hash
      state.authUser = user
      state.authPass = pass
      if (datasource) {
        ls.setItem(`${datasource}_user`, user)
        ls.setItem(`${datasource}_pass`, pass)
      }
    },
    setDataSources (state, {data}) {
      const datasources = []
      const engines = {}
      const auths = {}
      const metadataServices = {}
      data.forEach(d => {
        Object.entries(d).forEach(([key, val]) => {
          datasources.push(key)
          engines[key] = val.engines
          auths[key] = val.auth
          metadataServices[key] = val.metadataService
        })
      })
      state.datasources = datasources
      state.engines = engines
      state.auths = auths
      state.metadataServices = metadataServices

      ls.setItemJson('engines', engines)
      ls.setItemJson('auths', auths)
      ls.setItemJson('metadataServices', metadataServices)
    },
    setHash (state) {
      const hashString = location.hash.remove('#')
      const params = Object.fromQueryString(hashString, {deep: true, auth: false})

      const newHash = initialHash()
      for (const k of HASH_KEYS) {
        const [key, required] = k
        if (params[key]) {
          newHash[key] = params[key]
        } else if (required && state.hash[key]) {
          newHash[key] = state.hash[key]
        }
      }

      state.hash = newHash
    },
    setHashItem (state, data) {
      const validData = {}
      for (const k of HASH_KEYS) {
        const key = k[0]
        if (data[key] !== undefined) {
          validData[key] = data[key]
        }
      }
      state.hash = Object.assign({}, state.hash, validData)

      if (data.datasource && state.datasources.includes(data.datasource)) {
        ls.setItem('datasource', data.datasource)
      }
      if (data.engine) {
        ls.setItem('engine', data.engine)
      }
    },
    setIsModal (state, isOpen) {
      state.isModal = isOpen
    },
    setIsSettingOpen (state, isOpen) {
      state.isSettingOpen = isOpen
      ls.setItemBoolean('setting', state.isSettingOpen)
    },
    setIsSideHistoryOpen (state, isOpen) {
      state.isSideHistoryOpen = isOpen
      ls.setItemBoolean('panel', state.isSideHistoryOpen)
    },
    setSettings (state, data) {
      const update = {}
      for (const [key, val] of Object.entries(data)) {
        if (settings[key]) {
          update[key] = val
        }
      }

      state.settings = Object.assign({}, state.settings, update)

      for (const [key, val] of Object.entries(update)) {
        if (settings[key].type === 'boolean') {
          ls.setItemBoolean(settings[key].lsKey, val)
        } else {
          ls.setItem(settings[key].lsKey, val)
        }
      }
    },
    loadLocalStorage (state) {
      const update = {}
      for (const [key, val] of Object.entries(settings)) {
        switch (val.type) {
          case 'boolean':
            update[key] = ls.getItemBoolean(val.lsKey, val.default)
            break
          case 'number':
            update[key] = ls.getItemNumber(val.lsKey, val.default)
            break
          default:
            update[key] = ls.getItem(val.lsKey, val.default)
            break
        }
      }
      state.settings = update

      if (state.settings.rememberDatasource) {
        const datasource = ls.getItem('datasource')
        if (datasource) {
          state.hash = Object.assign({}, state.hash, {datasource})
        }
      }
      if (state.settings.rememberEngine) {
        const engine = ls.getItem('engine')
        if (engine) {
          state.hash = Object.assign({}, state.hash, {engine})
        }
      }

      state.isSettingOpen = ls.getItemBoolean('setting', false)
      state.isSideHistoryOpen = ls.getItemBoolean('panel', false)
    },
    loadAuth (state) {
      const {datasource} = state.hash
      if (datasource) {
        state.authUser = ls.getItem(`${datasource}_user`)
        state.authPass = ls.getItem(`${datasource}_pass`)
      }
    },
    loadComplete (state) {
      state.unload = false
    }
  },
  actions: {
    async getDataSources ({commit}) {
      const data = await api.getDataSources()
      if (data.datasources && data.datasources.length) {
        commit('setDataSources', {data: data.datasources})
      } else {
        location.replace('/error/?403')
      }
    },
    async testAuth ({state, getters}, {user, password}) {
      const {datasource} = state.hash
      const {isPresto, isHive, isSpark} = getters

      const auth = {user, password}

      let data
      if (isPresto) {
        data = await api.testAuthPresto(datasource, auth)
      } else if (isHive) {
        data = await api.testAuthHive(datasource, auth)
      } else if (isSpark) {
        data = await api.testAuthSpark(datasource, auth)
      } else {
        throw new Error('not supported')
      }

      if (data.results && data.results.length) {
        toastr.success('You can access.', 'Success', {
          positionClass: 'toast-top-right'
        })
      } else {
        const error = data.error || 'Error'
        toastr.error(error, 'Fail', {
          positionClass: 'toast-top-right'
        })
        throw error
      }
    }
  }
})

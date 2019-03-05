import Push from 'push.js'
import * as api from '@/api'
import {calcPrestoQueryProgress} from '@/utils'

const AUTH_ERROR_MESSAGE = 'auth may have expired. Please reload.'

function desktopNotification (success, query) {
  const title = success ? 'Done (Click here)' : 'Error'
  const config = {
    body: query.compact().truncate(64),
    icon: 'favicon.ico',
    timeout: 60 * 60 * 1000
  }

  if (success) {
    config.onClick = function () {
      window.focus()
      this.close()
    }
  }

  if (document.visibilityState !== 'visible') {
    Push.create(title, config)
  }
}

const state = () => {
  return {
    isPretty: false,

    runningQueryid: null,
    runningProgress: -1,
    runningTime: '',
    runningQueries: 0,

    queryString: '',

    loading: false,
    error: null,
    response: null,

    comment: {
      edit: true,
      update: null,
      user: null,
      content: null,
      like: 0
    },
    inputComment: '',
    visibleComment: false,
    inputLable: null,
    label: null,
    editLabel: false
  }
}

const getters = {}

const actions = {
  async runQuery ({commit, dispatch, state, getters, rootState, rootGetters}, option) {
    const {datasource, engine} = rootState.hash
    const {authInfo, isPresto, isHive, isSpark, isElasticsearch} = rootGetters
    const query = option && option.query ? option.query : rootState.editor.inputQuery
    const translateFlag = option && option.translateFlag ? option.translateFlag : false
    const enableDesktopNotification = rootState.settings.desktopNotification

    commit('initComment')
    commit('setHashItem', {queryid: '', chart: 0, pivot: 0, line: 0}, {root: true})
    commit('incrementRunningQueries')
    commit('setLoading', {data: true})
    commit('setError', {data: null})

    let error = null
    try {
      let data
      if (isPresto) {
        data = await api.runQueryPresto(datasource, query, authInfo)
      } else if (isHive) {
        data = await api.runQueryHive(datasource, query, authInfo)
      } else if (isSpark) {
        data = await api.runQuerySpark(datasource, query, authInfo)
      } else if (isElasticsearch) {
        if (translateFlag) {
          data = await api.translateQueryElasticsearch(datasource, query, authInfo)
        } else {
          data = await api.runQueryElasticsearch(datasource, query, authInfo)
        }
      } else {
        throw new Error('not supported')
      }

      const queryid = data.queryid
      if (queryid) {
        try {
          await dispatch('waitQueryComplete', {datasource, queryid, isPresto, isHive, isSpark, isElasticsearch})
          await dispatch('waitHistoryComplete', {datasource, engine, queryid})
          commit('setHashItem', {queryid: queryid, engine: data.engine}, {root: true})

          if (enableDesktopNotification) {
            desktopNotification(true, query)
          }
        } catch (e) {
          error = e || AUTH_ERROR_MESSAGE

          if (enableDesktopNotification) {
            desktopNotification(false, query)
          }
        }
      } else {
        error = data.error
      }
    } catch (e) {
      error = e || AUTH_ERROR_MESSAGE
    }

    commit('decrementRunningQueries')
    commit('setError', {data: error})
    if (error) {
      commit('setLoading', {data: false})
    }
  },
  async waitQueryComplete ({commit, rootGetters}, {datasource, queryid, isPresto, isHive, isSpark, isElasticsearch}) {
    const {authInfo, authUserInfo} = rootGetters
    const period = isPresto || isElasticsearch ? 500 : 5000

    commit('setRunningQueryId', {data: queryid})
    commit('setRunningProgress', {data: -1})
    commit('setRunningTime', {data: ''})

    let timer
    return new Promise((resolve, reject) => {
      timer = setInterval(async () => {
        try {
          let data
          if (isPresto) {
            data = await api.getQueryStatusPresto(datasource, queryid, authInfo)
          } else if (isHive) {
            data = await api.getQueryStatusHive(datasource, queryid, authUserInfo)
          } else if (isSpark) {
            data = await api.getQueryStatusSpark(datasource, queryid, authUserInfo)
          } else if (isElasticsearch) {
            data = await api.getQueryStatusElasticsearch(datasource, queryid, authUserInfo)
          } else {
            throw new Error('not supported')
          }

          const queryState = data.state
          if (queryState === 'FINISHED' || queryState === 'FAILED' || queryState === 'KILLED' || Object.isEmpty(data)) {
            resolve(data)
          } else if (queryState === 'RUNNING') {
            if (isPresto) {
              const stats = data.queryStats
              commit('setRunningProgress', {data: calcPrestoQueryProgress(stats, 1)})
              commit('setRunningTime', {data: stats.elapsedTime})
            } else if (isHive || isSpark) {
              commit('setRunningProgress', {data: data.progress})
              commit('setRunningTime', {data: (data.elapsedTime / 1000).ceil(1) + 's'})
            } else if (isElasticsearch) {
              commit('setRunningProgress', {data: 0})
              commit('setRunningTime', {data: 0})
            } else {
              throw new Error('not supported')
            }
          }
        } catch (e) {
          reject(e)
        }
      }, period)
    }).then(data => {
      clearInterval(timer)
      commit('setRunningQueryId', {data: ''})
      commit('setRunningProgress', {data: 0})
      commit('setRunningTime', {data: ''})

      const queryState = data.state

      if (queryState === 'FINISHED' || Object.isEmpty(data)) {
        commit('history/setHistoryId', {datasource, historyId: queryid}, {root: true})
      } else if (queryState === 'FAILED' || queryState === 'KILLED') {
        if (isPresto) {
          if (data.failureInfo.errorLocation) {
            commit('editor/setGotoLine', {line: data.failureInfo.errorLocation.lineNumber}, {root: true})
            commit('editor/setError', {
              line: data.failureInfo.errorLocation.lineNumber - 1,
              text: data.failureInfo.message
            }, {root: true})
          }
        }
      }

      return data
    }).catch(() => {
      clearInterval(timer)
    })
  },
  async waitHistoryComplete (context, {datasource, engine, queryid}) {
    let timer
    return new Promise((resolve, reject) => {
      timer = setInterval(async () => {
        try {
          const data = await api.getHistoryStatus(datasource, engine, queryid)

          if (data.error) {
            reject(data.error)
            return
          }
          if (data.status === 'ok') {
            resolve(data)
          }
        } catch (e) {
          reject(e)
        }
      }, 1000)
    }).then(data => {
      clearInterval(timer)
      return data
    }).catch(() => {
      clearInterval(timer)
    })
  },
  async loadQuery ({commit, dispatch, rootState}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!datasource || !engine || !queryid) {
      return false
    }

    dispatch('getComment')
    dispatch('getLabel')

    commit('setLoading', {data: true})
    commit('setError', {data: null})
    commit('setHashItem', {bookmark_id: ''}, {root: true})

    try {
      const data = await api.getQueryResult(datasource, engine, queryid)

      if (!Object.isEmpty(data) && data.queryString) {
        commit('setQueryString', {data: data.queryString})
        commit('editor/setInputQuery', {data: data.queryString.remove(/^EXPLAIN( \(TYPE DISTRIBUTED\)| \(TYPE VALIDATE\)| \(FORMAT GRAPHVIZ\)| ANALYZE|) /i)}, {root: true})
        commit('setEditLabel', {data: data.editLabel})

        if (!data.error) {
          if (data.results && /^show partitions /i.test(data.queryString)) {
            data.results.sortBy(r => r[0], true)
          }
          commit('setResponse', {data})
        } else {
          commit('setError', {data: data.error})
        }
      } else {
        commit('setQueryString', {data: ''})
      }
    } catch (e) {
      commit('setError', {data: e || AUTH_ERROR_MESSAGE})
    }

    commit('setLoading', {data: false})
  },
  async killQuery ({rootState, rootGetters}, {queryid}) {
    const {datasource} = rootState.hash
    const {isPresto, isHive, isSpark, authInfo, authUserInfo} = rootGetters
    if (isPresto) {
      return api.killQueryPresto(datasource, queryid, authInfo)
    } else if (isHive || isSpark) {
      return api.killQueryHive(datasource, queryid, authUserInfo)
    } else {
      throw new Error('not supported')
    }
  },
  async getComment ({commit, rootState}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!datasource || !engine || !queryid) {
      return false
    }

    commit('initComment')

    const data = await api.getComment(datasource, engine, queryid)

    if (data.comments && data.comments.length) {
      const comment = data.comments[0]
      commit('setComment', {
        edit: false,
        update: comment.updateTimeString,
        user: comment.user,
        content: comment.content,
        like: comment.likeCount
      })
      commit('setInputComment', {data: comment.content})
      commit('setVisibleComment', {data: true})
    }
  },
  async postComment ({commit, state, rootState}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!queryid) {
      return false
    }

    const data = await api.postComment(datasource, engine, queryid, state.inputComment)

    if (data.updateTimeString) {
      commit('setComment', {
        edit: false,
        update: data.updateTimeString,
        user: data.user,
        content: data.content,
        like: data.likeCount
      })
    }
  },
  async postCommentLike ({commit, state, rootState}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!queryid) {
      return false
    }

    commit('setComment', {like: state.comment.like + 1})

    await api.postCommentLike(datasource, engine, queryid, 1)
  },
  async deleteComment ({commit, rootState}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!queryid) {
      return false
    }

    await api.deleteComment(datasource, engine, queryid)
    commit('initComment')
  },
  async postLabel ({commit, state, rootState}, {inputLabel}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!datasource || !engine || !queryid) {
      return false
    }

    const data = await api.postLabel(datasource, engine, queryid, inputLabel)

    if (data.labelName) {
      commit('setLabel', {data: data.labelName})
    }
  },
  async deleteLabel ({commit, rootState}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!datasource || !engine || !queryid) {
      return false
    }

    await api.deleteLabel(datasource, engine, queryid)
    commit('setLabel', {data: null})
  },
  async getLabel ({commit, rootState}) {
    const {datasource, engine, queryid} = rootState.hash

    if (!datasource || !engine || !queryid) {
      return false
    }

    commit('initLabel')

    const data = await api.getLabel(datasource, engine, queryid)
    if (data.label) {
      commit('setLabel', {data: data.label})
    }
  },
  async publish ({rootState}) {
    const {datasource, engine, queryid} = rootState.hash
    const data = await api.publish(datasource, engine, queryid)
    return data.publish_id
  }
}

const mutations = {
  init (state) {
    state.runningQueryid = null
    state.runningQueries = 0
    state.queryString = ''
    state.response = null
  },
  setIsPretty (state, {data}) {
    state.isPretty = data
  },
  setRunningQueryId (state, {data}) {
    state.runningQueryid = data
  },
  setRunningProgress (state, {data}) {
    state.runningProgress = data
  },
  setRunningTime (state, {data}) {
    state.runningTime = data
  },
  setQueryString (state, {data}) {
    state.queryString = data
  },
  setResponse (state, {data}) {
    state.response = data
  },
  incrementRunningQueries (state) {
    state.runningQueries++
  },
  decrementRunningQueries (state) {
    state.runningQueries--
  },
  setLoading (state, {data}) {
    state.loading = data
  },
  setError (state, {data}) {
    state.error = data
  },
  initComment (state) {
    state.comment = {
      edit: true,
      update: null,
      user: null,
      content: null,
      like: 0
    }
    state.inputComment = ''
    state.visibleComment = false
  },
  setComment (state, comment) {
    state.comment = Object.assign({}, state.comment, comment)
  },
  setInputComment (state, {data}) {
    state.inputComment = data
  },
  setVisibleComment (state, {data}) {
    state.visibleComment = data
  },
  initLabel (state) {
    state.label = null
    state.editLabel = false
  },
  setLabel (state, {data}) {
    state.label = data
  },
  setEditLabel (state, {data}) {
    state.editLabel = data
  },
  reset (state) {
    state.queryString = ''
    state.response = null
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

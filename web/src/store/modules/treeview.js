import toastr from 'toastr'
import * as api from '@/api'
import {DATE_COLUMN_NAMES} from '@/constants'

const defaultCatalog = 'hive'

const state = () => {
  return {
    catalogs: [],
    schemata: [],
    tables: [],

    catalog: '',
    schema: '',
    table: '',
    note: '',
    meta: '',
    tableType: '',
    columns: [],
    partitionValues: {},
    selectedPartitions: {},

    tableQuery: '',
    filterSchema: '',
    filterTable: '',

    loadingPartitions: false,
    loadingTableSearch: false,
    tableSearchResponse: []
  }
}

const getters = {
  dateColumn (state) {
    return state.columns.map(c => c[0]).find(c => DATE_COLUMN_NAMES.includes(c)) || ''
  },
  otherColumns (state) {
    return state.columns.map(c => c[0]).filter(c => !DATE_COLUMN_NAMES.includes(c))
  },
  partitionKeys (state) {
    return state.columns.filter(c => c[2] === 'partition key').map(c => c[0])
  },
  partitionKeysTypes (state) {
    return state.columns.filter(c => c[2] === 'partition key').map(c => c[1])
  },
  columnTypesMap (state) {
    return state.columns.reduce((map, c) => Object.assign(map, {[c[0]]: c[1]}), {})
  }
}

const actions = {
  async getRoot ({dispatch, state, rootGetters}) {
    const {isPresto, isElasticsearch} = rootGetters
    const {catalog, schema, table} = state

    if (isElasticsearch) {
      dispatch('getTables')
      return
    }

    if (isPresto && !catalog) {
      dispatch('getCatalogs')
    } else if (!schema) {
      dispatch('getSchemata')
    } else if (!table) {
      dispatch('getTables')
    }
  },
  async getCatalogs ({commit, dispatch, state, rootState, rootGetters}) {
    const {datasource} = rootState.hash
    const {authInfo, isPresto} = rootGetters

    if (!isPresto) {
      throw Error('getCatalogs are not supported except for presto')
    }

    const data = await api.getCatalogsPresto(datasource, authInfo)

    if (data.results && data.results.length) {
      commit('setCatalogs', {data: data.results.map(r => r[0])})
      if (state.catalogs.includes(defaultCatalog)) {
        commit('setCatalog', {data: defaultCatalog})
      } else {
        commit('setCatalog', {data: state.catalogs[0]})
      }
    } else {
      commit('setCatalogs', {data: []})
      if (data.error) {
        toastr.error(data.error)
      }
    }
  },
  async getSchemata ({commit, state, rootState, rootGetters}) {
    const {datasource} = rootState.hash
    const {authInfo, isPresto, isHive, isSpark} = rootGetters
    const {catalog} = state

    let data
    if (isPresto) {
      data = await api.getSchemataPresto(datasource, catalog, authInfo)
    } else if (isHive) {
      data = await api.getSchemataHive(datasource, authInfo)
    } else if (isSpark) {
      data = await api.getSchemataSpark(datasource, authInfo)
    } else {
      throw new Error('not supported')
    }

    if (data.results && data.results.length) {
      commit('setSchemata', {data: data.results.map(r => r[0])})
      commit('setSchema', {data: state.schemata[0]})
    } else {
      commit('setSchemata', {data: []})
      if (data.error) {
        toastr.error(data.error)
      }
    }
  },
  async getTables ({commit, state, rootState, rootGetters}) {
    const {datasource} = rootState.hash
    const {authInfo, isPresto, isHive, isSpark, isElasticsearch} = rootGetters
    const {catalog, schema} = state

    let data
    if (isPresto) {
      data = await api.getTablesPresto(datasource, catalog, schema, authInfo)
    } else if (isHive) {
      data = await api.getTablesHive(datasource, schema, authInfo)
    } else if (isSpark) {
      data = await api.getTablesSpark(datasource, schema, authInfo)
    } else if (isElasticsearch) {
      data = await api.getTablesElasticsearch(datasource, authInfo)
    } else {
      throw new Error('not supported')
    }

    if (data.results && data.results.length) {
      commit('setTables', {
        data: data.results.map(r => {
          const tableName = isSpark ? r[1] : r[0]
          const tableType = isPresto ? r[1] : 'BASE TABLE'
          return [tableName, tableType]
        })
      })
    } else {
      commit('setTables', {data: []})
      if (data.error) {
        toastr.error(data.error)
      }
    }
  },
  async getColumns ({commit, state, rootState, rootGetters}) {
    const {datasource} = rootState.hash
    const {authInfo, isPresto, isHive, isSpark, isElasticsearch} = rootGetters
    const {catalog, schema, table} = state

    if (isPresto && !catalog) {
      return false
    }

    if (!isElasticsearch && !schema) {
      return false
    }

    if (!table) {
      return false
    }

    let data
    if (isPresto) {
      data = await api.getColumnsPresto(datasource, catalog, schema, table, authInfo)
    } else if (isHive) {
      data = await api.getColumnsHive(datasource, schema, table, authInfo)
    } else if (isSpark) {
      data = await api.getColumnsSpark(datasource, schema, table, authInfo)
    } else if (isElasticsearch) {
      data = await api.getColumnsElasticsearch(datasource, table, authInfo)
    } else {
      throw new Error('not supported')
    }

    if (data.results && data.results.length) {
      let columns = data.results
      const note = data.note
      const meta = data.meta
      if (!isPresto) {
        columns = columns
          .filter(c => c[0] && !c[0].includes('#'))
          .reduce((arr, c) => {
            const index = arr.findIndex(existColumn => existColumn[0] === c[0])
            if (index === -1) {
              arr.push([c[0], c[1], null, null])
            } else {
              arr[index][2] = 'partition key'
            }
            return arr
          }, [])
      }
      commit('setColumns', {columns, note, meta})
    } else {
      commit('setColumns', {data: []})
      if (data.error) {
        toastr.error(data.error)
      }
    }
  },
  async getPartitions ({commit, state, getters, rootState, rootGetters}) {
    const {datasource} = rootState.hash
    const {authInfo, isPresto, isHive, isSpark} = rootGetters
    const {catalog, schema, table, selectedPartitions} = state
    const {partitionKeys, columnTypesMap} = getters

    const option = {}

    if (Object.keys(selectedPartitions).length === Object.keys(partitionKeys).length) {
      return
    }

    let remainKey = ''
    if (!Object.isEmpty(selectedPartitions)) {
      for (const key of partitionKeys) {
        if (selectedPartitions[key]) {
          remainKey = key
        } else {
          break
        }
      }
    }
    commit('deletePartitionValues', {keys: partitionKeys, remainKey})

    if (!Object.isEmpty(selectedPartitions)) {
      const keys = []
      const vals = []
      for (const key of partitionKeys) {
        if (!selectedPartitions[key]) {
          break
        }
        keys.push(key)
        vals.push(selectedPartitions[key])
      }
      option.partitionColumn = keys.join(',')
      option.partitionColumnType = keys.map(k => columnTypesMap[k]).join(',')
      option.partitionValue = vals.join(',')
    }

    commit('setLoadingPartitions', {loading: true})

    let data
    if (isPresto) {
      data = await api.getPartitionsPresto(datasource, catalog, schema, table, option, authInfo)
    } else if (isHive) {
      data = await api.getPartitionsHive(datasource, schema, table, option, authInfo)
    } else if (isSpark) {
      data = await api.getPartitionsSpark(datasource, schema, table, option, authInfo)
    } else {
      throw new Error('not supported')
    }

    if (data.error) {
      toastr.error(data.error)
    } else if (data.column && data.partitions) {
      commit('setPartitionValues', {keys: partitionKeys, key: data.column, values: data.partitions})
    }

    commit('setLoadingPartitions', {loading: false})
  },
  async searchTable ({commit, state, rootState, rootGetters}) {
    const {datasource} = rootState.hash
    const {catalog, tableQuery} = state
    const {authInfo} = rootGetters

    commit('setTable', {data: ['', '']})
    commit('setTableSearchResponse', {data: []})

    if (tableQuery === '') {
      return false
    }

    commit('setLoadingTableSearch', {loading: true})

    try {
      const data = await api.searchTable(datasource, catalog, tableQuery, authInfo)
      commit('setTableSearchResponse', {data: data.results})
      commit('setLoadingTableSearch', {loading: false})
    } catch (e) {
      commit('setLoadingTableSearch', {loading: false})
    }
  }
}

const mutations = {
  init (state) {
    state.catalogs = []
    state.schemata = []
    state.tables = []
    state.catalog = ''
    state.schema = ''
    state.table = ''
    state.note = ''
    state.meta = ''
    state.tableType = ''
    state.columns = []
    state.partitionValues = {}
    state.tableQuery = ''
    state.filterSchema = ''
    state.filterTable = ''
    state.tableSearchResponse = []
  },
  setCatalogs (state, {data}) {
    state.catalogs = data
  },
  setSchemata (state, {data}) {
    state.schemata = data
  },
  setTables (state, {data}) {
    state.tables = data
  },
  setCatalog (state, {data}) {
    state.catalog = data
  },
  setSchema (state, {data}) {
    state.schema = data
  },
  setTable (state, {data}) {
    state.table = data[0]
    state.tableType = data[1]
  },
  setColumns (state, {columns, note, meta}) {
    state.columns = columns
    state.note = note
    state.meta = meta
  },
  setPartitionValues (state, {keys, key, values}) {
    const newVal = {}
    for (const k of keys) {
      if (k === key) {
        break
      }
      newVal[k] = state.partitionValues[k]
    }
    newVal[key] = values
    state.partitionValues = newVal
  },
  deletePartitionValues (state, {keys, remainKey}) {
    if (!remainKey) {
      state.partitionValues = {}
    }
    const newVal = {}
    for (const k of keys) {
      newVal[k] = state.partitionValues[k]
      if (k === remainKey) {
        break
      }
    }
    state.partitionValues = newVal
  },
  setSelectedPartitions (state, {data}) {
    state.selectedPartitions = data
  },
  setTableQuery (state, {data}) {
    state.tableQuery = data
  },
  setFilterSchema (state, {data}) {
    state.filterSchema = data
  },
  setFilterTable (state, {data}) {
    state.filterTable = data
  },
  setLoadingPartitions (state, {loading}) {
    state.loadingPartitions = loading
  },
  setLoadingTableSearch (state, {loading}) {
    state.loadingTableSearch = loading
  },
  setTableSearchResponse (state, {data}) {
    state.tableSearchResponse = data
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}

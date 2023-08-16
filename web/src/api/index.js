import axios from 'axios'
import toastr from 'toastr'
import {HIDDEN_QUERY_PREFIX} from '@/constants'

const BASE_URL = process.env.BASE_URL
const TIMEOUT = 300000

const axiosConfig = {
  baseURL: BASE_URL
}

if (process.env.NODE_ENV !== 'production') {
  axiosConfig['headers'] = {
    'X-yanagishima-datasources': '*'
  }
}

const client = axios.create(axiosConfig)

client.interceptors.request.use(config => {
  // console.log('=== axios request ===')
  // console.log(config)
  return config
})

client.interceptors.response.use(response => {
  // console.log('=== axios response ===')
  // console.log(response)
  return response
}, error => {
  switch (error.response.status) {
    case 403:
      location.replace('/error/?403')
      break
    case 500:
      toastr.error('Please inform to admin', '500')
      break
  }
  return error
})

function makeFormParams (data) {
  return Object.entries(data).reduce((params, [key, val]) => {
    params.append(key, val)
    return params
  }, new URLSearchParams())
}

const apis = {
  datasource: '/datasource',
  datasourceAuth: '/datasourceAuth',
  tableList: '/tableList',
  presto: '/presto',
  prestoAsync: '/prestoAsync',
  prestoQuery: '/prestoQuery',
  prestoQueryStatus: '/prestoQueryStatus',
  trino: '/trino',
  trinoAsync: '/trinoAsync',
  trinoQuery: '/trinoQuery',
  trinoQueryStatus: '/trinoQueryStatus',
  history: '/history',
  historyStatus: '/historyStatus',
  queryHistory: '/queryHistory?datasource={datasource}',
  download: '/download?datasource={datasource}&queryid={queryid}&encode=UTF-8&header={includeHeader}',
  csvdownload: '/csvdownload?datasource={datasource}&queryid={queryid}&encode=UTF-8&header={includeHeader}',
  publish: '/publish',
  bookmark: '/bookmark',
  format: '/format',
  convertPresto: '/convertPresto',
  convertHive: '/convertHive',
  killPresto: '/killPresto',
  killTrino: '/killTrino',
  prestoDetail: '/prestoQueryDetail?datasource={datasource}&queryid={queryid}',
  trinoDetail: '/trinoQueryDetail?datasource={datasource}&queryid={queryid}',
  shareHistory: '/share/shareHistory',
  shareDownload: '/share/download?publish_id={publishId}&encode=UTF-8&header={includeHeader}',
  shareCsvDownload: '/share/csvdownload?publish_id={publishId}&encode=UTF-8&header={includeHeader}',
  toValuesQuery: '/toValuesQuery',
  hive: '/hive',
  hiveAsync: '/hiveAsync',
  hiveQueryStatus: '/hiveQueryStatus',
  yarnJobList: '/yarnJobList',
  killHive: '/killHive',
  hiveQueryDetail: '/hiveQueryDetail?engine=hive&datasource={datasource}&id={id}',
  prestoPartition: '/prestoPartition',
  trinoPartition: '/trinoPartition',
  hivePartition: '/hivePartition',
  queryHistoryUser: '/queryHistoryUser?datasource={datasource}&engine={engine}',
  bookmarkUser: '/bookmarkUser',
  comment: '/comment',
  spark: '/spark',
  sparkPartition: '/sparkPartition',
  sparkAsync: '/sparkAsync',
  sparkQueryStatus: '/sparkQueryStatus',
  sparkQueryDetail: '/sparkQueryDetail?engine=spark&datasource={datasource}',
  sparkJobList: '/sparkJobList',
  starredSchema: '/starredSchema'
}

function addHiddenQueryPrefix (query) {
  return HIDDEN_QUERY_PREFIX + query
}

export async function testAuthPresto (datasource, authInfo) {
  const params = {
    datasource,
    query: 'SELECT -1',
    ...authInfo
  }
  const response = await client.post(apis.presto, makeFormParams(params))
  return response.data
}

export async function testAuthHive (datasource, authInfo) {
  const params = {
    engine: 'hive',
    datasource,
    query: 'SELECT -1',
    ...authInfo
  }
  const response = await client.post(apis.hive, makeFormParams(params))
  return response.data
}

export async function testAuthSpark (datasource, authInfo) {
  const params = {
    engine: 'spark',
    datasource,
    query: 'SELECT -1',
    ...authInfo
  }
  const response = await client.post(apis.spark, makeFormParams(params))
  return response.data
}

export async function getDataSources () {
  const response = await client.get(apis.datasourceAuth)
  return response.data
}

export async function getCatalogsPresto (datasource, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix('SHOW CATALOGS'),
    ...authInfo
  }
  const response = await client.post(apis.presto, makeFormParams(params))
  return response.data
}

export async function getCatalogsTrino (datasource, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix('SHOW CATALOGS'),
    ...authInfo
  }
  const response = await client.post(apis.trino, makeFormParams(params))
  return response.data
}

export async function getSchemataPresto (datasource, catalog, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix(`SHOW SCHEMAS FROM ${catalog}`),
    ...authInfo
  }
  const response = await client.post(apis.presto, makeFormParams(params))
  return response.data
}

export async function getSchemataTrino (datasource, catalog, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix(`SHOW SCHEMAS FROM ${catalog}`),
    ...authInfo
  }
  const response = await client.post(apis.trino, makeFormParams(params))
  return response.data
}

export async function getSchemataHive (datasource, authInfo) {
  const params = {
    engine: 'hive',
    datasource,
    query: 'SHOW SCHEMAS',
    ...authInfo
  }
  const response = await client.post(apis.hive, makeFormParams(params))
  return response.data
}

export async function getSchemataSpark (datasource, authInfo) {
  const params = {
    engine: 'spark',
    datasource,
    query: 'SHOW SCHEMAS',
    ...authInfo
  }
  const response = await client.post(apis.spark, makeFormParams(params))
  return response.data
}

export async function getStarredSchemata (datasource, engine, catalog) {
  const params = {
    datasource,
    engine,
    catalog
  }
  const response = await client.get(apis.starredSchema, {params})
  return response.data
}

export async function postStarredSchema (datasource, engine, catalog, schema) {
  const params = {
    datasource,
    engine,
    catalog,
    schema
  }
  const response = await client.post(apis.starredSchema, makeFormParams(params))
  return response.data
}

export async function deleteStarredSchema (datasource, engine, catalog, id) {
  const params = {
    datasource,
    engine,
    catalog,
    starred_schema_id: id
  }
  const response = await client.delete(apis.starredSchema, {params})
  return response.data
}

export async function getTablesPresto (datasource, catalog, schema, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix(
      `SELECT table_name, table_type FROM ${catalog}.information_schema.tables
               WHERE table_schema='${schema}' ORDER BY table_name`),
    ...authInfo
  }
  const response = await client.post(apis.presto, makeFormParams(params))
  return response.data
}

export async function getTablesTrino (datasource, catalog, schema, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix(
      `SELECT table_name, table_type FROM ${catalog}.information_schema.tables
               WHERE table_schema='${schema}' ORDER BY table_name`),
    ...authInfo
  }
  const response = await client.post(apis.trino, makeFormParams(params))
  return response.data
}

export async function getTablesHive (datasource, schema, authInfo) {
  const params = {
    engine: 'hive',
    datasource,
    query: `SHOW TABLES IN ${schema}`,
    ...authInfo
  }
  const response = await client.post(apis.hive, makeFormParams(params))
  return response.data
}

export async function getTablesSpark (datasource, schema, authInfo) {
  const params = {
    engine: 'spark',
    datasource,
    query: `SHOW TABLES IN ${schema}`,
    ...authInfo
  }
  const response = await client.post(apis.spark, makeFormParams(params))
  return response.data
}

export async function getColumnsPresto (datasource, catalog, schema, table, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix(`DESCRIBE ${catalog}.${schema}."${table}"`),
    ...authInfo
  }
  const response = await client.post(apis.presto, makeFormParams(params))
  return response.data
}

export async function getColumnsTrino (datasource, catalog, schema, table, authInfo) {
  const params = {
    datasource,
    query: addHiddenQueryPrefix(`DESCRIBE ${catalog}.${schema}."${table}"`),
    ...authInfo
  }
  const response = await client.post(apis.trino, makeFormParams(params))
  return response.data
}

export async function getColumnsHive (datasource, schema, table, authInfo) {
  const params = {
    engine: 'hive',
    datasource,
    query: `DESCRIBE ${schema}.\`${table}\``,
    ...authInfo
  }
  const response = await client.post(apis.hive, makeFormParams(params))
  return response.data
}

export async function getColumnsSpark (datasource, schema, table, authInfo) {
  const params = {
    engine: 'spark',
    datasource,
    query: `DESCRIBE ${schema}.${table}`,
    ...authInfo
  }
  const response = await client.post(apis.spark, makeFormParams(params))
  return response.data
}

export async function getPartitionsPresto (datasource, catalog, schema, table, option, authInfo) {
  const params = {
    datasource,
    catalog,
    schema,
    table,
    ...option,
    ...authInfo
  }
  const response = await client.post(apis.prestoPartition, makeFormParams(params))
  return response.data
}

export async function getPartitionsTrino (datasource, catalog, schema, table, option, authInfo) {
  const params = {
    datasource,
    catalog,
    schema,
    table,
    ...option,
    ...authInfo
  }
  const response = await client.post(apis.trinoPartition, makeFormParams(params))
  return response.data
}

export async function getPartitionsHive (datasource, schema, table, option, authInfo) {
  const params = {
    engine: 'hive',
    datasource,
    schema,
    table,
    ...option,
    ...authInfo
  }
  const response = await client.post(apis.hivePartition, makeFormParams(params))
  return response.data
}

export async function getPartitionsSpark (datasource, schema, table, option, authInfo) {
  const params = {
    engine: 'spark',
    datasource,
    schema,
    table,
    ...option,
    ...authInfo
  }
  const response = await client.post(apis.sparkPartition, makeFormParams(params))
  return response.data
}

export async function searchTable (datasource, catalog, query, authInfo) {
  const q = `SELECT table_catalog, table_schema, table_name, table_type FROM ${catalog}.information_schema.tables WHERE table_name LIKE '%${query}%'`
  const params = {
    datasource,
    query: addHiddenQueryPrefix(q),
    ...authInfo
  }
  const response = await client.post(apis.presto, makeFormParams(params))
  return response.data
}

export async function getTableList (datasource, catalog, authInfo) {
  const params = {
    datasource,
    catalog,
    ...authInfo
  }
  const response = await client.post(apis.tableList, makeFormParams(params))
  return response.data
}

export async function runQueryPresto (datasource, query, authInfo) {
  const params = {
    datasource,
    query,
    ...authInfo
  }
  const response = await client.post(apis.prestoAsync, makeFormParams(params))
  return response.data
}

export async function runQueryTrino (datasource, query, authInfo) {
  const params = {
    datasource,
    query,
    ...authInfo
  }
  const response = await client.post(apis.trinoAsync, makeFormParams(params))
  return response.data
}

export async function runQueryHive (datasource, query, authInfo) {
  const params = {
    engine: 'hive',
    datasource,
    query,
    ...authInfo
  }
  const response = await client.post(apis.hiveAsync, makeFormParams(params))
  return response.data
}

export async function runQuerySpark (datasource, query, authInfo) {
  const params = {
    engine: 'spark',
    datasource,
    query,
    ...authInfo
  }
  const response = await client.post(apis.sparkAsync, makeFormParams(params))
  return response.data
}

export async function getQueryStatusPresto (datasource, queryid, authInfo) {
  const params = {
    datasource,
    queryid,
    ...authInfo
  }
  const response = await client.post(apis.prestoQueryStatus, makeFormParams(params))
  return response.data
}

export async function getQueryStatusTrino (datasource, queryid, authInfo) {
  const params = {
    datasource,
    queryid,
    ...authInfo
  }
  const response = await client.post(apis.trinoQueryStatus, makeFormParams(params))
  return response.data
}

export async function getQueryStatusHive (datasource, queryid, authUserInfo) {
  const params = {
    engine: 'hive',
    datasource,
    queryid,
    ...authUserInfo
  }
  const response = await client.post(apis.hiveQueryStatus, makeFormParams(params))
  return response.data
}

export async function getQueryStatusSpark (datasource, queryid, authUserInfo) {
  const params = {
    engine: 'spark',
    datasource,
    queryid,
    ...authUserInfo
  }
  const response = await client.post(apis.sparkQueryStatus, makeFormParams(params))
  return response.data
}

export async function getHistoryStatus (datasource, engine, queryid) {
  const params = {
    datasource,
    engine,
    queryid
  }
  const response = await client.get(apis.historyStatus, {params})
  return response.data
}

export async function getQueryResult (datasource, engine, queryid) {
  const params = {
    datasource,
    engine,
    queryid
  }
  const response = await client.get(apis.history, {params, timeout: TIMEOUT})
  return response.data
}

export async function queryHistoryById (datasource, historyIds, label) {
  const url = apis.queryHistory.format({datasource})
  const params = {
    queryids: historyIds.join(','),
    label
  }
  const response = await client.post(url, makeFormParams(params), {timeout: TIMEOUT})
  return response.data
}

export async function queryHistoryUser (datasource, engine, filter, offset, limit, label) {
  const url = apis.queryHistoryUser.format({datasource, engine})
  const params = {
    search: filter,
    offset,
    limit,
    label
  }
  const response = await client.get(url, {params, timeout: TIMEOUT})
  return response.data
}

export async function killQueryPresto (datasource, queryid, authInfo) {
  const params = {
    datasource,
    queryid,
    ...authInfo
  }
  const response = await client.post(apis.killPresto, makeFormParams(params))
  return response.data
}

export async function killQueryTrino (datasource, queryid, authInfo) {
  const params = {
    datasource,
    queryid,
    ...authInfo
  }
  const response = await client.post(apis.killTrino, makeFormParams(params))
  return response.data
}

export async function killQueryHive (datasource, queryid, authUserInfo) {
  const params = {
    datasource,
    id: queryid,
    ...authUserInfo
  }
  const response = await client.post(apis.killHive, makeFormParams(params))
  return response.data
}

export async function formatQuery (query) {
  const params = {
    query
  }
  const response = await client.post(apis.format, makeFormParams(params))
  return response.data
}

export async function validateQuery (datasource, query) {
  const params = {
    datasource,
    query: `EXPLAIN (TYPE VALIDATE) ${query}`
  }
  const response = await client.post(apis.presto, makeFormParams(params))
  return response.data
}

export async function convertQueryToHive (query) {
  const params = {
    query
  }
  const response = await client.post(apis.convertHive, makeFormParams(params))
  return response.data
}

export async function convertQueryToPresto (query) {
  const params = {
    query
  }
  const response = await client.post(apis.convertPresto, makeFormParams(params))
  return response.data
}

export async function getBookmarksById (datasource, engine, bookmarkIds) {
  const params = {
    datasource,
    engine,
    bookmark_id: bookmarkIds.join(',')
  }
  const response = await client.get(apis.bookmark, {params, timeout: TIMEOUT})
  return response.data
}

export async function getBookmarksUser (datasource, engine) {
  const params = {
    datasource,
    engine
  }
  const response = await client.get(apis.bookmarkUser, {params, timeout: TIMEOUT})
  return response.data
}

export async function addBookmarkItem (datasource, engine, title, query) {
  const params = {
    datasource,
    engine,
    title,
    query
  }
  const response = await client.post(apis.bookmark, makeFormParams(params), {timeout: TIMEOUT})
  return response.data
}

export async function deleteBookmarkItem (datasource, engine, bookmarkId) {
  const params = {
    datasource,
    engine,
    bookmark_id: bookmarkId
  }
  const response = await client.delete(apis.bookmark, {params, timeout: TIMEOUT})
  return response.data
}

export async function getComment (datasource, engine, queryid) {
  const params = {
    datasource,
    engine,
    queryid
  }
  const response = await client.get(apis.comment, {params, timeout: TIMEOUT})
  return response.data
}

export async function postComment (datasource, engine, queryid, content) {
  const params = {
    datasource,
    engine,
    queryid,
    content
  }
  const response = await client.post(apis.comment, makeFormParams(params), {timeout: TIMEOUT})
  return response.data
}

export async function postCommentLike (datasource, engine, queryid, like) {
  const params = {
    datasource,
    engine,
    queryid,
    like
  }
  const response = await client.post(apis.comment, makeFormParams(params), {timeout: TIMEOUT})
  return response.data
}

export async function deleteComment (datasource, engine, queryid) {
  const params = {
    datasource,
    engine,
    queryid
  }
  const response = await client.delete(apis.comment, {params, timeout: TIMEOUT})
  return response.data
}

export async function publish (datasource, engine, queryid) {
  const params = {
    datasource,
    engine,
    queryid
  }
  const response = await client.post(apis.publish, makeFormParams(params))
  return response.data
}

export async function getTimeline (datasource, engine, filter) {
  const params = {
    datasource,
    engine,
    search: filter
  }
  const response = await client.get(apis.comment, {params, timeout: TIMEOUT})
  return response.data
}

export async function getQlistPresto (datasource, authInfo) {
  const params = {
    datasource,
    ...authInfo
  }
  const response = await client.post(apis.prestoQuery, makeFormParams(params))
  return response.data
}

export async function getQlistTrino (datasource, authInfo) {
  const params = {
    datasource,
    ...authInfo
  }
  const response = await client.post(apis.trinoQuery, makeFormParams(params))
  return response.data
}

export async function getQlistHive (datasource) {
  const params = {
    datasource
  }
  const response = await client.get(apis.yarnJobList, {params})
  return response.data
}

export async function getQlistSpark (datasource) {
  const params = {
    datasource
  }
  const response = await client.get(apis.sparkJobList, {params})
  return response.data
}

export async function getSharedQueryResult (publishId) {
  const params = {
    publish_id: publishId
  }
  const response = await client.get(apis.shareHistory, {params})
  return response.data
}

export function buildDownloadUrl (datasource, queryid, isCsv, includeHeader) {
  const api = isCsv ? apis.csvdownload : apis.download
  return BASE_URL + api.format({datasource, queryid, includeHeader})
}

export function buildShareDownloadUrl (publishId, isCsv, includeHeader) {
  const api = isCsv ? apis.shareCsvDownload : apis.shareDownload
  return BASE_URL + api.format({publishId, includeHeader})
}

export function buildDetailUrl (isPresto, isHive, isSpark, isTrino, datasource, queryid) {
  if (isPresto) {
    return BASE_URL + apis.prestoDetail.format({datasource, queryid})
  } else if (isHive) {
    return BASE_URL + apis.hiveQueryDetail.format({datasource, id: queryid})
  } else if (isSpark) {
    return BASE_URL + apis.sparkQueryDetail.format({datasource})
  } else if (isTrino) {
    return BASE_URL + apis.trinoDetail.format({datasource, queryid})
  } else {
    throw new Error('not supported')
  }
}

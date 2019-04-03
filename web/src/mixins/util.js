import $ from 'jquery'
import {HASH_KEYS} from '@/constants'
import * as utils from '@/utils'
import * as api from '@/api'

export default {
  methods: {
    buildTopUrl () {
      return location.protocol + '//' + location.host
    },
    buildUrl (hash) {
      return '#' + HASH_KEYS.filter(k => hash[k[0]]).map(k => `${k[0]}=${hash[k[0]]}`).join('&')
    },
    buildShareUrl (datasource, engine, queryid, chart, pivot, line) {
      return this.buildTopUrl() + '/' + this.buildUrl({datasource, engine, tab: 'result', queryid, chart, pivot, line})
    },
    buildDownloadUrl (datasource, queryid, isCsv, includeHeader) {
      return api.buildDownloadUrl(datasource, queryid, isCsv, includeHeader)
    },
    buildDetailUrl (isPresto, isHive, isSpark, datasource, queryid) {
      return api.buildDetailUrl(isPresto, isHive, isSpark, datasource, queryid)
    },
    buildShareDownloadUrl (publishId, isCsv, includeHeader) {
      return api.buildShareDownloadUrl(publishId, isCsv, includeHeader)
    },
    calcPrestoQueryProgress: utils.calcPrestoQueryProgress,
    showModal (id) {
      $(`#${id}`).modal('show')
    },
    hideModal (id) {
      $(`#${id}`).modal('hide')
    }
  }
}

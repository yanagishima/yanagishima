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
    buildDownloadUrl (datasource, queryid, isCsv) {
      return api.buildDownloadUrl(datasource, queryid, isCsv)
    },
    buildDetailUrl (isPresto, isHive, datasource, queryid) {
      return api.buildDetailUrl(isPresto, isHive, datasource, queryid)
    },
    buildShareDownloadUrl (publishId, isCsv) {
      return api.buildShareDownloadUrl(publishId, isCsv)
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

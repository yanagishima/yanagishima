<template>
  <div>
    <header id="header" style="background: #333;">
      <div id="header-main">
        <div class="container">
          <div id="header-upper" class="py-2">
            <div class="row align-items-center">
              <div class="col-6">
                <h1 id="logo"><a :href="buildTopUrl()"><span id="logo-figure" class="mr-2"></span>{{sitename}}</a><span class="ml-2 font-weight-normal">{{version}}</span></h1>
              </div>
              <div class="col-6 text-right">
                Readonly
              </div>
            </div>
          </div>
        </div>
      </div>
      <div id="header-sub">
        <div class="container">
          <div class="row align-items-center pt-3">
            <div class="col-9">
              <template v-if="loading">
                <strong>Loading</strong>
              </template>
              <template v-else>
                <template v-if="response.error || error">
                  <strong><i class="fa fa-exclamation-triangle text-danger mr-1"></i>Error</strong>
                </template>
                <template v-else>
                  <span v-if="response.results">
                    <span class="mr-3" v-if="response.lineNumber">
                      <i class="fas fa-file-alt" title="Publish ID" data-toggle="tooltip"
                         data-placement="left"></i>
                      <strong>{{publishId}}</strong>
                    </span>
                    <span class="mr-3" v-if="response.queryid">
                      <a :href="buildShareUrl(datasource, engine, queryid, chart, pivot, line)">{{queryid}}</a>
                    </span>
                    <span class="mr-2" v-if="response.finishedTime">
                      <i class="fa fa-calendar" title="Finished time" data-toggle="tooltip" data-animation="false"
                         data-placement="left"></i>
                      {{response.finishedTime | extractDate}}
                    </span>
                    <span class="mr-2" v-if="response.elapsedTimeMillis">
                      <strong>{{(response.elapsedTimeMillis / 1000).ceil(2)}}</strong><span
                      class="text-muted ml-1">sec</span>
                    </span>
                    <span class="mr-2" v-if="response.rawDataSize">
                      <strong>{{response.rawDataSize.remove('B')}}</strong><span class="text-muted ml-1">byte</span>
                    </span>
                    <span class="mr-2" v-if="response.lineNumber">
                      <strong>{{response.results.length | formatNumber}}</strong>
                      <template v-if="response.results.length !== response.lineNumber - 1">
                        <span class="mx-1">/</span><strong>{{response.lineNumber - 1 | formatNumber}}</strong>
                      </template>
                      <span class="text-muted ml-1">results</span>
                    </span>
                  </span>
                  <span class="mr-2" v-if="response.headers">
                    <strong>{{response.headers.length}}</strong><span class="text-muted ml-1">columns</span>
                  </span>
                  <span v-else><strong>No result</strong></span>
                </template>
              </template>
            </div>
            <div class="col-3 text-right">
              <label class="ml-2">
                <input type="checkbox" v-model="isPretty">
                Pretty print
              </label>
              <div class="btn-group ml-2" v-if="response.rawDataSize">
                <a href="#" data-toggle="dropdown">Download<i class="fa fa-fw fa-download ml-1"></i></a>
                <div class="dropdown-menu dropdown-menu-right">
                  <div class="dropdown-header">header</div>
                  <a :href="buildShareDownloadUrl(publishId, false, true)" class="dropdown-item">TSV</a>
                  <a :href="buildShareDownloadUrl(publishId, true, true)" class="dropdown-item">CSV</a>
                  <div class="dropdown-header">no header</div>
                  <a :href="buildShareDownloadUrl(publishId, false, false)" class="dropdown-item">TSV</a>
                  <a :href="buildShareDownloadUrl(publishId, true, false)" class="dropdown-item">CSV</a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>

    <div id="content">
      <div class="container">
        <template v-if="loading">
          <div class="alert alert-info" role="alert">
            <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Loading
          </div>
        </template>
        <template v-else>
          <template v-if="!Object.isEmpty(response)">
            <div class="editor p-2 mb-3">
              <BaseAce :code="response.queryString" :readonly="true" :max-lines="isQueryCollapse ? 4 : Infinity"
                   css_class="bg-transparent"></BaseAce>
              <div class="text-right" v-if="response.queryString.split('\n').length > 4">
                <a href="#" @click.prevent="isQueryCollapse = !isQueryCollapse">
                  Show
                  <template v-if="isQueryCollapse">More</template>
                  <template v-else>Less</template>
                </a>
              </div>
            </div>
            <div class="alert alert-info" v-if="line">
              <i class="fa fa-fw fa-check mr-1"></i><a href="#" class="alert-link" v-scroll-to="`#L${line}`">Line
              {{line}}</a> is selected
            </div>
            <div class="mb-3" v-if="enableChart && chart">
              <div class="card">
                <div class="card-header">
                  <strong>{{chartTypes[chart].name}}</strong>
                </div>
                <div class="card-block">
                  <vue-chart :chart-type="chartTypes[chart].type" :columns="chartColumns" :rows="chartRows"
                             :options="Object.assign({}, chartOptions, chartTypes[chart].option)"></vue-chart>
                  <div v-if="response.lineNumber > 501" class="text-right text-muted">
                    This data is only top 500.
                  </div>
                </div>
              </div>
            </div>
            <div class="mb-3" v-if="enablePivot && pivot">
              <div class="card">
                <div class="card-block">
                  <pivot :data="pivotRows" :fields="[]" :row-fields="rowFields" :col-fields="colFields" :reducer="reducer" :default-show-settings="false">
                  </pivot>
                  <div v-if="response.lineNumber > 501" class="text-right text-muted">
                    This data is only top 500.
                  </div>
                </div>
              </div>
            </div>
            <ResultTable :result="response" :pretty="isPretty" :line="line" :readonly="true"/>
          </template>
          <template v-else>
            <div class="alert alert-warning" role="alert">
              <div class="row align-items-center">
                <div class="col">
                  <i class="fa fa-fw fa-frown-o mr-1"></i>I'm sorry.
                </div>
              </div>
            </div>
          </template>
        </template>
      </div>
    </div>

    <footer id="footer" class="py-3">
      <address class="text-center">
        <span><strong>&copy; yanagishima</strong> by wyukawa and okazou</span>
      </address>
    </footer>

    <div id="comment" class="card" :class="{conpact: !visibleComment}" v-if="response && response.comment">
      <div class="card-header">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <template v-if="visibleComment">
              <i class="fa fa-commenting-o fa-flip-horizontal mr-1"></i>
              {{response.comment.updateTimeString | extractDate}}
            </template>
            <template v-else>
              <a href="#" @click.prevent="visibleComment = true">
                <i class="fa fa-commenting fa-flip-horizontal mr-1"></i>
                {{response.comment.updateTimeString | extractDate}}
              </a>
            </template>
          </div>
          <div v-if="visibleComment">
            <a href="#" @click.prevent="visibleComment = false"><i class="fa fa-fw fa-times"></i></a>
          </div>
        </div>
      </div>
      <div class="card-block">
        <div id="comment-body">
          <template v-if="response.comment.content">
            <pre class="comment"><BaseAutoLink :text="response.comment.content.escapeHTML()"></BaseAutoLink></pre>
          </template>
          <template v-else>
            <pre><span class="text-muted">(none)</span></pre>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ResultTable from '@/components/ResultTable'
import util from '@/mixins/util'
import chart from '@/mixins/chart'
import pivot from '@/mixins/pivot'
import * as api from '@/api'
import {SITENAME, VERSION, CHART_TYPES, CHART_OPTIONS} from '@/constants'
import Pivot from '@marketconnect/vue-pivot-table'
import $ from 'jquery'

export default {
  name: 'ShowView',
  components: {ResultTable, Pivot},
  mixins: [util, chart, pivot],
  data () {
    return {
      sitename: SITENAME,
      version: VERSION,
      publishId: '',
      datasource: '',
      engine: '',
      queryid: '',
      line: 0,
      chart: 0,
      pivot: 0,
      loading: true,
      response: null,
      error: null,
      chartTypes: CHART_TYPES,
      chartOptions: CHART_OPTIONS,
      isQueryCollapse: false,
      visibleComment: false,
      isPretty: false
    }
  },
  computed: {
    disabledDownload () {
      return Object.isEmpty(this.response) || !this.publishId || this.loading
    }
  },
  created () {
    const params = location.search.remove('?').split('&')
    let publishId = ''
    let chart = 0
    let pivot = 0
    for (const param of params) {
      if (/^[a-z0-9]{32}$/.test(param)) {
        publishId = param
      } else if (param.length === 1) {
        chart = Number(param)
      } else if (param === 'pivot') {
        pivot = 1
      }
    }
    const line = Number(location.hash.remove('#L')) || 0

    if (publishId) {
      this.publishId = publishId
      document.title = `#${publishId} - ${SITENAME}`
      api.getSharedQueryResult(publishId)
        .then(data => {
          this.datasource = data.datasource
          this.engine = data.engine
          this.queryid = data.queryid
          this.response = data
          this.line = line
          this.chart = chart
          this.pivot = pivot
          this.loading = false
          this.visibleComment = data.comment != null
          this.$store.commit('loadComplete')
        })
        .catch(() => {
        })
    }
  },
  mounted () {
    $('body').tooltip({selector: '[data-toggle="tooltip"]'})
  }
}
</script>

<style scoped>
</style>

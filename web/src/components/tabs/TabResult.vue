<template>
  <div>
    <div class="header row align-items-center pt-3">
      <div class="col-8">
        <template v-if="loading">
          <template v-if="runningProgress">
            <strong>Running</strong>
            <span class="ml-2">{{runningQueryid}}</span>
          </template>
          <strong v-else>Loading</strong>
        </template>
        <template v-else>
          <span class="mr-3" v-if="response && response.error || error">
            <strong class="text-danger mr-2">
              <i class="fa fa-exclamation-triangle text-danger mr-1"></i>Error
            </strong>
            <span class="text-muted">{{queryid}}</span>
          </span>
          <span class="mr-3" v-else-if="response && response.results">
            <i class="fa fa-file-text-o" title="query ID" data-toggle="tooltip" data-animation="false"
                data-placement="left"></i>
            <strong>{{queryid}}</strong>
          </span>
          <span class="mr-3" v-else><strong>No result</strong></span>
          <span class="mr-2" v-if="response && response.finishedTime">
            <i class="fa fa-calendar" title="Finished time" data-toggle="tooltip" data-animation="false"
                data-placement="left"></i>
            {{response.finishedTime | extractDate}}
          </span>
          <span class="mr-2 d-md-none d-lg-inline" v-if="response && response.elapsedTimeMillis">
            <strong>{{(response.elapsedTimeMillis / 1000).ceil(2)}}</strong><span
            class="text-muted ml-1">sec</span>
          </span>
          <span class="mr-2 d-md-none d-lg-inline" v-if="response && response.rawDataSize">
            <strong>{{response.rawDataSize.remove('B')}}</strong><span class="text-muted ml-1">byte</span>
          </span>
          <span class="mr-2" v-if="response && response.results && response.lineNumber">
            <strong>{{response.results.length | formatNumber}}</strong>
            <template v-if="response.results.length !== response.lineNumber - 1">
              <span class="mx-1">/</span>
              <strong>{{response.lineNumber - 1 | formatNumber}}</strong>
            </template>
            <span class="text-muted ml-1">results</span>
          </span>
          <span class="mr-2 d-md-none d-lg-inline" v-if="response && response.headers">
            <strong>{{response.headers.length}}</strong><span class="text-muted ml-1">columns</span>
          </span>
        </template>
      </div>
      <template v-if="response && response.results">
        <div class="col-4 text-right">
          <label>
            <input type="checkbox" v-model="isPrettyModel" :disabled="actionDisabled" class="align-middle">
            Pretty print
          </label>
          <div class="btn-group mx-2">
            <a :href="buildDownloadUrl(datasource, queryid, isCsv, includeHeader)" class="btn btn-sm btn-secondary"
               :class="{disabled: actionDisabled}" data-toggle="tooltip" data-html="true"
               data-animation="false" title="Can change format in <i class='fa fa-cog'></i> setting"><i class="fa fa-fw fa-download"></i><span class="d-md-none d-lg-inline ml-1">Download</span></a>
          </div>
          <button class="btn btn-sm btn-secondary" :disabled="actionDisabled" @click="publish"
                  data-toggle="tooltip" data-animation="false" title="Publish Readonly Result">
            <i class="fa fa-fw fa-globe"></i><span class="d-md-none d-lg-inline ml-1">Publish</span>
          </button>
        </div>
      </template>
    </div>
    <template v-if="loading">
      <template v-if="runningProgress !== -1">
        <div v-if="runningTime" class="alert alert-info">
          <div class="row align-items-center">
            <div class="col-2 text-right">
              <strong>{{runningTime}}</strong>
            </div>
            <div class="col">
              <div class="progress">
                <div class="progress-bar progress-bar-striped progress-bar-animated bg-info flex-row"
                     role="progressbar" :style="`width: ${runningProgress}%`">
                  <template v-if="runningProgress"><strong>{{runningProgress}}</strong>%</template>
                </div>
              </div>
            </div>
            <div class="col-2">
              <div class="btn-group">
                <a href="#" class="btn btn-sm btn-secondary" @click.prevent="killQuery"><i
                  class="fa fa-fw fa-times mr-1 text-danger"></i>Kill</a>
                <a class="btn btn-sm btn-secondary" :href="buildDetailUrl(isPresto, isHive, isSpark, isTrino, datasource, runningQueryid)"
                   :target="'_blank'"><i class="fa fa-fw fa-info"></i>Info</a>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="alert alert-info">
          <i class="fa fa-fw fa-spinner fa-spin mr-1"></i>Preparing
        </div>
      </template>
      <template v-else>
        <div class="alert alert-info">
          <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Loading
        </div>
      </template>
    </template>
    <template v-else>
      <template v-if="queryString">
        <template v-if="error">
          <div class="alert alert-danger">
            <pre class="pb-3">{{error}}</pre>
          </div>
        </template>
        <template v-else>
          <template v-if="response.error">
            <div class="alert alert-danger">
              <pre class="pb-3">{{response.error}}</pre>
            </div>
          </template>
          <template v-else>
            <template v-if="/^EXPLAIN /i.test(queryString)">
              <template v-if="isPresto && /^EXPLAIN \(FORMAT GRAPHVIZ\)/i.test(queryString)">
                <template v-if="explainGraphResult">
                  <a href="#lity-svg-image" data-lity>
                    <img :src="`data:image/svg+xml,${encodeURIComponent(explainGraphResult)}`" class="img-fluid">
                  </a>
                  <div id="lity-svg-image" class="lity-hide" style="background:#fff; overflow: auto;">
                    <img :src="`data:image/svg+xml,${encodeURIComponent(explainGraphResult)}`">
                  </div>
                </template>
              </template>
              <template v-else>
                <pre id="explain"><code v-html="explainTextResult"></code></pre>
              </template>
            </template>
            <template v-else>
              <template v-if="/^SELECT VIEW_DEFINITION FROM hive.INFORMATION_SCHEMA.VIEWS/i.test(queryString)">
                <div class="card">
                  <div class="card-header">
                    PRESTO VIEW DDL
                  </div>
                  <div class="card-body">
                    <BaseAce :code="response.results[0][0]" :readonly="true" :max-lines="Infinity"></BaseAce>
                  </div>
                </div>
              </template>
              <template v-else-if="/^SHOW CREATE TABLE/i.test(queryString)">
                <div class="card">
                  <div class="card-header">
                    SHOW CREATE TABLE
                  </div>
                  <div class="card-body">
                    <BaseAce :code="createTableResult" :readonly="true" :max-lines="Infinity"></BaseAce>
                  </div>
                </div>
              </template>
              <template v-else>
                <div v-if="line" class="alert alert-info">
                  <button class="close" @click="resetLine"><span>&times;</span></button>
                  <i class="fa fa-fw fa-check mr-1"></i>
                  <a href="#" class="alert-link" v-scroll-to="'#L' + line">Line {{line}}</a> is selected
                </div>
                <template v-if="response.results">
                  <div v-if="enableChart" class="mb-3">
                    <div v-if="chart" class="card">
                      <div class="card-header">
                        <div class="row align-items-center">
                          <div class="col">
                            <div v-for="(val, key) in validChartTypes" :key="key" class="form-check form-check-inline">
                              <input class="form-check-input" type="radio" :id="`chart${val.name}Radio`" v-model="chartModel" :value="key">
                              <label class="form-check-label" :for="`chart${val.name}Radio`">{{val.name}}</label>
                            </div>
                          </div>
                          <div class="col-1">
                            <button type="button" class="close" @click="setChart(0)"><span>&times;</span></button>
                          </div>
                        </div>
                      </div>
                      <div class="card-body">
                        <vue-chart :key="chart" :chart-type="validChartTypes[chart].type" :columns="chartColumns" :rows="chartRows"
                                   :options="Object.assign({}, chartOptions, validChartTypes[chart].option)" />
                        <div v-if="response.lineNumber > 501" class="text-right text-muted">
                          This data is only top 500.
                        </div>
                      </div>
                    </div>
                    <a v-else href="#" @click.prevent="setChart(1)">
                      <i class="fa fa-fw fa-plus-square mr-1"></i>Chart
                    </a>
                  </div>
                  <div v-if="enablePivot" class="mb-3">
                    <div v-if="pivot" class="card">
                      <div class="card-header">
                        <button type="button" class="close" @click="setPivot(0)"><span>&times;</span></button>
                        <div class="card-body">
                          <pivot :data="pivotRows" :fields="[]" :row-fields="rowFields" :col-fields="colFields" :reducer="reducer" :default-show-settings="false">
                          </pivot>
                          <div v-if="response.lineNumber > 501" class="text-right text-muted">
                            This data is only top 500.
                          </div>
                        </div>
                      </div>
                    </div>
                    <a v-else href="#" @click.prevent="setPivot(1)"><i class="fa fa-fw fa-plus-square mr-1"></i>Pivot(Beta)</a>
                  </div>
                  <ResultTable :result="response" :pretty="isPretty" :line="line" @line-click="toggleLine"/>
                </template>
              </template>
            </template>
          </template>
        </template>
      </template>
    </template>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import toastr from 'toastr'
import ResultTable from '@/components/ResultTable'
import util from '@/mixins/util'
import chart from '@/mixins/chart'
import pivot from '@/mixins/pivot'
import {CHART_TYPES, CHART_OPTIONS} from '@/constants'
import Pivot from '@marketconnect/vue-pivot-table'

export default {
  name: 'TabResult',
  components: {ResultTable, Pivot},
  mixins: [util, chart, pivot],
  data () {
    return {
      chartTypes: CHART_TYPES,
      chartOptions: CHART_OPTIONS,
      explainGraphResult: null,
      inputLabel: null
    }
  },
  computed: {
    ...mapState({
      datasource: state => state.hash.datasource,
      queryid: state => state.hash.queryid,
      line: state => state.hash.line,
      chart: state => state.hash.chart,
      pivot: state => state.hash.pivot,
      isCsv: state => state.settings.isCsv,
      includeHeader: state => state.settings.includeHeader
    }),
    ...mapGetters([
      'isPresto',
      'isHive',
      'isSpark',
      'isTrino',
      'datasourceEngine'
    ]),
    ...mapState('result', [
      'isPretty',
      'runningQueryid',
      'runningProgress',
      'runningTime',
      'queryString',
      'loading',
      'response',
      'error',
      'editLabel'
    ]),
    isPrettyModel: {
      get () {
        return this.isPretty
      },
      set (val) {
        this.$store.commit('result/setIsPretty', {data: val})
      }
    },
    chartModel: {
      get () {
        return this.chart
      },
      set (val) {
        this.setChart(val)
      }
    },
    actionDisabled () {
      return this.response.error || !this.queryid || this.loading
    },
    explainTextResult () {
      const r = this.response
      if (r && r.results && /^EXPLAIN /i.test(this.queryString)) {
        if (this.isPresto && /^EXPLAIN \(FORMAT GRAPHVIZ\)/i.test(this.queryString)) {
          // graph explain
          return null
        } else {
          const lines = r.results.map(r => r[0].replace(/ {4}/g, ' '))
          return lines.join('\n')
        }
      } else {
        return null
      }
    },
    createTableResult () {
      if (!this.queryString || !/^SHOW CREATE TABLE/i.test(this.queryString)) {
        return null
      }

      if (this.response && this.response.results) {
        const arr = this.response.results.map(r => r[0])
        return arr.join('\n')
      } else {
        return ''
      }
    }
  },
  watch: {
    async response (val) {
      if (val && val.results && this.isPresto && /^EXPLAIN \(FORMAT GRAPHVIZ\)/i.test(this.queryString)) {
        try {
          const [{default: Viz}, {Module, render}] = await Promise.all([
            import(/* webpackChunkName: "viz" */ 'viz.js'),
            import(/* webpackChunkName: "viz" */ 'viz.js/full.render.js')
          ])
          const viz = new Viz({Module, render})
          this.explainGraphResult = await viz.renderString(val.results[0][0])
        } catch (e) {
          console.error(e)
        }
      } else {
        this.explainGraphResult = null
      }
    }
  },
  created () {
    if (!this.loading) {
      this.$store.dispatch('result/loadQuery')
    }
  },
  methods: {
    toggleLine (line) {
      if (line === this.line) {
        line = 0
      }
      this.$store.commit('setHashItem', {line})
    },
    resetLine () {
      this.$store.commit('setHashItem', {line: 0})
    },
    setChart (chart) {
      this.$store.commit('setHashItem', {chart})
    },
    setPivot (pivot) {
      this.$store.commit('setHashItem', {pivot})
    },
    killQuery () {
      this.$store.dispatch('result/killQuery', {queryid: this.runningQueryid})
    },
    publish () {
      this.$store.dispatch('result/publish')
        .then(publishId => {
          if (!publishId) {
            return
          }
          let path = `/share/?${publishId}`
          if (this.chart) {
            path += `&${this.chart}`
          }
          if (this.pivot) {
            path += '&pivot'
          }
          if (this.line) {
            path += `#L${this.line}`
          }
          const ua = navigator.userAgent.toLowerCase()
          if (ua.indexOf('chrome') !== -1 && navigator.clipboard) {
            navigator.clipboard.writeText(`${location.protocol}//${location.host}${path}`)
          }
          toastr.success(this.queryid, 'Published (Click Here)', {
            onclick: function () {
              window.open(path, '_blank')
            }
          })
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
</style>

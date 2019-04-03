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
            </div>
          </div>
        </div>
      </div>
      <div id="header-sub">
        <div class="container">
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
          <table class="table table-bordered table-fixed table-hover">
            <thead>
              <th>version1</th>
              <th>version2</th>
            </thead>
            <tbody>
              <tr>
                <td><a :href="buildTopUrl() + buildUrl({datasource, engine, tab: 'result', queryid: queryid1})" target="_blank">{{queryid1}}</a></td>
                <td><a :href="buildTopUrl() + buildUrl({datasource, engine, tab: 'result', queryid: queryid2})" target="_blank">{{queryid2}}</a></td>
              </tr>
            </tbody>
          </table>
          <div>
            <vueCodeDiff :old-string="queryString1" :new-string="queryString2" :context="context" :output-format="outputFormat"/>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import vueCodeDiff from 'vue-code-diff'
import util from '@/mixins/util'
import * as api from '@/api'
import {SITENAME, VERSION} from '@/constants'

export default {
  name: 'DiffView',
  components: {vueCodeDiff},
  mixins: [util],
  data () {
    return {
      sitename: SITENAME,
      version: VERSION,
      datasource: '',
      engine: '',
      queryid1: '',
      queryid2: '',
      queryString1: '',
      queryString2: '',
      outputFormat: 'side-by-side',
      context: 10,
      loading: true
    }
  },
  created () {
    const params = this.$route.query
    this.datasource = params['datasource']
    this.engine = params['engine']
    this.queryid1 = params['queryid1']
    this.queryid2 = params['queryid2']
    const promise1 = api.getQueryResult(this.datasource, this.engine, this.queryid1)
    const promise2 = api.getQueryResult(this.datasource, this.engine, this.queryid2)
    Promise.all([promise1, promise2]).then(values => {
      this.queryString1 = values[0].queryString
      this.queryString2 = values[1].queryString
      this.loading = false
      this.$store.commit('loadComplete')
    }).catch(() => {
    })
  }
}
</script>

<style scoped>
</style>

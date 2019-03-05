<template>
  <div>
    <div class="header row align-items-center pt-3">
      <div class="col">
        <span><strong class="mr-2">{{datasource}}</strong>as of<strong class="ml-2">{{now}}</strong></span>
      </div>
      <div class="col text-right">
        <label class="form-check-label mr-3" v-if="isPresto">
          <input class="form-check-input mr-1" type="checkbox" v-model="isOpenQueryModel">Full Query
        </label>
        <label class="form-check-label mr-3">
          <input class="form-check-input mr-1" type="checkbox" v-model="isAdminModeModel">Admin Mode
        </label>
        <button class="btn btn-sm btn-secondary" @click="getQlist">
          <i class="fa fa-fw fa-sync mr-1"></i>Refresh
        </button>
      </div>
    </div>

    <div>
      <template v-if="loading">
        <div class="alert alert-info">
          <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Loading
        </div>
      </template>
      <template v-else>
        <div v-if="filterUser !== ''" class="alert alert-info">
          <button class="close" @click="filterUser = ''"><span>&times;</span></button>
          <span class="mr-3">Filter by <strong>{{filterUser}}</strong></span>
          <span class="mr-2"><strong>{{orderedQlist.length}}</strong> Results</span>
          <span class="mr-2" v-if="orderedFailQlist.length"><strong>{{orderedFailQlist.length}}</strong> Fails</span>
        </div>
        <template v-if="isPresto">
          <table v-if="orderedQlist.length" class="table table-bordered table-fixed table-hover">
            <thead>
            <tr>
              <th width="7%">State</th>
              <th width="15%">query ID</th>
              <th width="5%" class="text-right">Elapsed</th>
              <th width="40%">Query</th>
              <th width="12.5%">Source</th>
              <th width="12.5%">User</th>
              <th width="4%" class="text-center">Kill</th>
              <th width="4%" class="text-center">Info</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(item, i) in orderedQlist" :key="i"
                :class="{'table-danger': item.state === 'FAILED', 'table-info': isRunning(item.state)}">
              <td>
                <template v-if="item.state === 'RUNNING'">
                  <div class="progress">
                    <div class="progress-bar bg-info" :style="`width: ${calcPrestoQueryProgress(item.queryStats)}%`">
                      <small>{{calcPrestoQueryProgress(item.queryStats)}}%</small>
                    </div>
                  </div>
                </template>
                <template v-else>
                  {{item.state.camelize()}}
                </template>
              </td>
              <td>
                <template v-if="item.existdb && (item.state === 'FINISHED' || item.state === 'FAILED')">
                  <a :href="buildUrl({datasource, engine, tab: 'result', queryid: item.queryId})">{{item.queryId}}</a>
                </template>
                <template v-else>
                  {{item.queryId}}
                </template>
              </td>
              <td class="text-right">{{item.queryStats.elapsedTime | formatUnit}}</td>
              <td class="ace-font">
                <pre v-if="isOpenQuery" class="ace-font mb-0">{{item.query}}</pre>
                <span :title="item.query" v-else>{{item.query.compact()}}</span>
              </td>
              <td>{{item.session.source}}</td>
              <td><a href="#" @click.prevent="filterUser = item.session.user">{{item.session.user}}</a></td>
              <td class="text-center"><a v-if="isRunning(item.state)" href="#" @click.prevent="killQuery(item.queryId)"
                                         class="btn btn-sm btn-secondary p-1"><i
                class="fa fa-fw fa-times text-danger"></i></a></td>
              <td class="text-center">
                <a target="_blank" class="btn btn-sm btn-secondary p-1"
                   :href="buildDetailUrl(isPresto, isHive, isSpark, datasource, item.queryId)"><i class="fa fa-fw fa-info"></i></a>
              </td>
            </tr>
            </tbody>
          </table>
          <div class="alert alert-warning" v-else>
            <i class="fa fa-fw fa-frown-o mr-1"></i>No result
          </div>
        </template>
        <template v-else-if="isHive">
          <table v-if="orderedQlist.length" class="table table-bordered table-fixed table-hover">
            <thead>
            <tr>
              <th width="7%">State</th>
              <th width="20%">application ID</th>
              <th width="5%" class="text-right">Elapsed</th>
              <th width="51.5%">Name</th>
              <th width="12.5%">User</th>
              <th width="4%" class="text-center">Info</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(item, i) in orderedQlist" :key="i"
                :class="{'table-danger': item.state === 'Failed', 'table-info': isRunning(item.state)}">
              <td>
                <template v-if="item.state === 'RUNNING'">
                  <div class="progress">
                    <div class="progress-bar bg-info" :style="`width: ${item.progress}%`">
                      <small>{{item.progress.ceil()}}%</small>
                    </div>
                  </div>
                </template>
                <template v-else>
                  {{item.finalStatus === 'UNDEFINED' ? item.state.camelize() : item.finalStatus.camelize()}}
                </template>
              </td>
              <td>
                {{item.id}}
              </td>
              <td class="text-right">{{item.elapsedTime | formatUnit}}</td>
              <td class="ace-font">
                <template v-if="item.existdb && (item.state === 'FINISHED' || item.state === 'FAILED')">
                  <a :href="buildUrl({datasource, engine, tab: 'result', queryid: getHiveQueryid(item.name)})">{{item.name}}</a>
                </template>
                <template v-else>
                  {{item.name}}
                </template>
              </td>
              <td>{{item.user}}</td>
              <td class="text-center"><a target="_blank" class="btn btn-sm btn-secondary p-1"
                                         :href="buildDetailUrl(isPresto, isHive, isSpark, datasource, item.id)"><i
                class="fa fa-fw fa-info"></i></a></td>
            </tr>
            </tbody>
          </table>
          <div class="alert alert-warning" v-else>
            <i class="fa fa-fw fa-frown-o mr-1"></i>No result
          </div>
        </template>
        <template v-else-if="isSpark">
          <table v-if="orderedQlist.length" class="table table-bordered table-fixed table-hover">
            <thead>
            <tr>
              <th width="5%">status</th>
              <th width="10%">jobIds</th>
              <th width="7%">submissionTime</th>
              <th width="5%" class="text-right">Elapsed</th>
              <th width="64%">query</th>
              <th width="5%">user</th>
              <th width="4%" class="text-center">Info</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(item, i) in orderedQlist" :key="i"
                :class="{'table-danger': item.status === 'FAILED', 'table-info': isRunning(item.status)}">
              <td>
                <template v-if="item.status === 'RUNNING'">
                  <div class="progress">
                    <div class="progress-bar bg-info" :style="`width: ${item.progress}%`">
                      <small>{{item.progress.ceil()}}%</small>
                    </div>
                  </div>
                </template>
                <template v-else>
                  {{item.status.camelize()}}
                </template>
              </td>
              <td>{{item.jobIds}}</td>
              <td>{{item.submissionTime}}</td>
              <td class="text-right">{{item.duration}}</td>
              <td class="ace-font">
                <span :title="item.query" v-if="item.query">{{item.query.compact()}}</span>
              </td>
              <td><a href="#" @click.prevent="filterUser = item.user">{{item.user}}</a></td>
              <td class="text-center"><a target="_blank" class="btn btn-sm btn-secondary p-1"
                                         :href="buildDetailUrl(isPresto, isHive, isSpark, datasource)"><i
                class="fa fa-fw fa-info"></i></a></td>
            </tr>
            </tbody>
          </table>
          <div class="alert alert-warning" v-else>
            <i class="fa fa-fw fa-frown-o mr-1"></i>No result
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import util from '@/mixins/util'
import {HIDDEN_QUERY_PREFIX} from '@/constants'

export default {
  name: 'TabQlist',
  mixins: [util],
  data () {
    return {
      filterUser: ''
    }
  },
  computed: {
    ...mapState([
      'isSuperAdminMode'
    ]),
    ...mapState({
      datasource: state => state.hash.datasource,
      engine: state => state.hash.engine,
      isAutoQlist: state => state.settings.isAutoQlist
    }),
    ...mapState('qlist', [
      'now',
      'isOpenQuery',
      'isAdminMode',
      'loading',
      'response',
      'error'
    ]),
    ...mapGetters([
      'isPresto',
      'isHive',
      'isSpark',
      'isElasticsearch'
    ]),
    isOpenQueryModel: {
      get () {
        return this.isOpenQuery
      },
      set (val) {
        this.$store.commit('qlist/setIsOpenQuery', {data: val})
      }
    },
    isAdminModeModel: {
      get () {
        return this.isAdminMode
      },
      set (val) {
        this.$store.commit('qlist/setIsAdminMode', {data: val})
      }
    },
    orderedQlist () {
      if (this.error) {
        return []
      }

      let qlist
      if (this.isPresto) {
        qlist = this.response.filter(q => {
          if (this.filterUser === '' || this.filterUser === q.session.user) {
            if (this.isSuperAdminMode) {
              return true
            }
            if (!q.query.includes(HIDDEN_QUERY_PREFIX)) {
              if (this.isAdminMode) {
                return true
              }
              if (q.existdb || (q.session.source === 'yanagishima' && q.state !== 'FINISHED')) {
                return true
              }
            }
          }
          return false
        })
      } else if (this.isHive) {
        qlist = this.response.filter(q => q.name.includes('yanagishima') || this.isAdminMode || this.isSuperAdminMode)
      } else if (this.isSpark) {
        qlist = this.response.filter(q => this.filterUser === '' || this.filterUser === q.user)
      } else {
        throw new Error('not supported')
      }

      // place running queries first
      const finished = []
      const running = []
      for (const q of qlist) {
        if (this.isRunning(q.state)) {
          running.push(q)
        } else {
          finished.push(q)
        }
      }
      return running.concat(finished)
    },
    orderedFailQlist () {
      return this.response.filter(q => this.filterUser === '' || (this.isPresto && this.filterUser === q.session.user && q.state === 'FAILED'))
    }
  },
  watch: {
    isAutoQlist (val) {
      this.$store.dispatch('qlist/autoQlist', {enable: val})
    }
  },
  created () {
    if (this.isAutoQlist) {
      this.$store.dispatch('qlist/autoQlist', {enable: true})
    } else {
      this.$store.dispatch('qlist/getQlist', {isAutoQlist: false})
    }
  },
  methods: {
    getQlist () {
      this.$store.dispatch('qlist/getQlist', {isAutoQlist: false})
    },
    isRunning (state) {
      if (this.isPresto) {
        return !['FINISHED', 'FAILED', 'CANCELED'].includes(state)
      } else if (this.isHive) {
        return !['FINISHED', 'FAILED', 'KILLED'].includes(state)
      } else if (this.isSpark) {
        return !['FINISHED', 'FAILED'].includes(state)
      } else {
        throw new Error('not supported')
      }
    },
    killQuery (queryid) {
      if (confirm('Do you want to do it?')) {
        this.$store.dispatch('result/killQuery', {queryid})
      }
    },
    getHiveQueryid (applicationId) {
      if (applicationId.includes('yanagishima-hive')) {
        return applicationId.split('-').last()
      } else {
        return applicationId
      }
    }
  }
}
</script>

<style scoped>
</style>

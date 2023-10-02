<template>
  <div>
    <div class="header row align-items-center pt-3">
      <div class="col">
        <strong class="mr-1">Executed History</strong>
        <template v-if="isLocalStorage">
          <span v-if="filteredHistory.length">{{filteredHistory.length}}</span>
        </template>
        <template v-else>
          <template v-if="response && response.total">
            <span>{{response.hit}}</span>
            <template v-if="response.total !== response.hit">
              /
              <span>{{response.total}}</span>
              <span class="text-muted ml-1">({{(response.hit / response.total * 100).round()}}%)</span>
            </template>
          </template>
        </template>
        <template v-if="label">
          <button type="button" class="btn btn-sm btn-secondary" disabled>{{label}}</button>
          <button type="button" class="btn btn-sm btn-secondary" @click="clearLabel"><i class="fa fa-fw fa-times mr-1"></i></button>
        </template>
      </div>
      <div class="col text-right">
        <template v-if="isLocalStorage">
          <input type="text" class="form-control form-control-sm d-inline-block w-50"
                 placeholder="Filter by Query" v-model="filterModel" v-focus>
        </template>
        <template v-else>
          <input type="text" class="form-control form-control-sm d-inline-block w-50"
                 placeholder="Search by Query" v-model.lazy="filterModel" v-focus @keyup.enter="getHistories(false)">
        </template>
      </div>
    </div>

    <div>
      <template v-if="loading">
        <div class="alert alert-info">
          <i class="fa fa-fw fa-spinner fa-pulse mr-1"></i>Loading
        </div>
      </template>
      <template v-else>
        <table v-if="filteredHistory.length" class="table table-sm table-bordered table-fixed table-hover">
          <thead>
          <tr>
            <th width="4%" class="text-right md-none">No</th>
            <th width="20%">
              <div class="d-flex align-items-center justify-content-between">
                <p class="my-0">query ID</p>
                <div class="custom-control custom-switch md-none">
                  <input type="checkbox" class="custom-control-input" id="compareSwitch" v-model="isCompareMode">
                  <label class="custom-control-label font-weight-normal" for="compareSwitch">Compare</label>
                </div>
              </div>
            </th>
            <th width="12%" class="md-none">Finished</th>
            <th width="75" class="text-right">Elapsed</th>
            <th>Query</th>
            <th width="45" class="text-center">DL</th>
            <th width="45" class="text-center">Info</th>
          </tr>
          </thead>
          <tbody>
            <tr v-for="(h, i) in filteredHistory" :key="i" class="vertical-top">
              <td class="text-right text-muted md-none">
                {{i + 1}}
              </td>
              <td>
                <template v-if="isCompareMode">
                  <input type="checkbox" class="mr-1" :value="h[0]" v-model="checkedQueries" :disabled="checkedQueries.length >= 2 && !checkedQueries.includes(h[0])" @change="compare"/>
                </template>
                <a :href="buildUrl({datasource, engine, tab: 'result', queryid: h[0]})">{{h[0]}}</a>
              </td>
              <td class="md-none">
                {{h[5] | extractDate}}
              </td>
              <td class="text-right">
                {{(h[2] / 1000).ceil(2)}}s
              </td>
              <td class="td-hover">
                <button class="btn btn-sm btn-secondary set" @click.prevent="setQurey(h[1])">
                  <i class="far fa-fw fa-keyboard"></i>
                </button>
                <pre class="ace-font mb-0"><BaseHighlight :sentence="h[1].escapeHTML()" :keyword="filter.escapeHTML()"></BaseHighlight></pre>
              </td>
              <td class="text-center">
                <div class="btn-group">
                  <a v-if="h[3] !== '0B'" :href="buildDownloadUrl(datasource, h[0], isCsv, includeHeader)" data-toggle="tooltip" :title="h[3]"><i
                    class="fa fa-fw fa-download"></i></a>
                </div>
              </td>
              <td class="text-center">
                <a target="_blank" class="btn btn-sm btn-secondary p-1"
                  :href="buildDetailUrl(isPresto, isHive, isSpark, isTrino, datasource, h[0])"><i class="fa fa-fw fa-info"></i></a>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="alert alert-warning">
          <i class="fa fa-fw fa-frown-o mr-1"></i>No result
        </div>
        <div v-if="!isLocalStorage && hasMore" class="p-3 text-center">
          <button type="button" class="btn btn-primary" @click="getHistories(true)">More</button>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import util from '@/mixins/util'

export default {
  name: 'TabHistory',
  mixins: [util],
  data () {
    return {
      checkedQueries: [],
      isCompareMode: false
    }
  },
  computed: {
    ...mapState({
      isLocalStorage: state => state.settings.isLocalStorage,
      datasource: state => state.hash.datasource,
      engine: state => state.hash.engine,
      isCsv: state => state.settings.isCsv,
      includeHeader: state => state.settings.includeHeader
    }),
    ...mapState('history', [
      'filter',
      'loading',
      'response',
      'label'
    ]),
    ...mapGetters('history', [
      'hasMore'
    ]),
    ...mapGetters([
      'isPresto',
      'isHive',
      'isSpark',
      'isTrino',
      'datasourceEngine'
    ]),
    filterModel: {
      get () {
        return this.filter
      },
      set (val) {
        this.$store.commit('history/setFilter', {data: val})
      }
    },
    filteredHistory () {
      if (!(this.response && this.response.results)) {
        return []
      }

      const history = this.response.results.filter(h => h[4] === this.engine)

      if (this.isLocalStorage) {
        const filter = this.filter.trim()
        const filters = filter.includes(' ') ? filter.split(' ').unique() : [filter]
        return history.filter(h => filters.every(f => new RegExp(RegExp.escape(f), 'ig').test(h[1])))
      } else {
        return history
      }
    }
  },
  mounted () {
    this.getHistories()
  },
  methods: {
    getHistories (isMore) {
      this.$store.dispatch('history/getHistories', {isMore})
    },
    setQurey (query) {
      this.$store.commit('editor/setInputQuery', {data: query})
      this.$store.commit('editor/focusOnEditor')
    },
    clearLabel () {
      this.$store.commit('history/setLabel', {data: null})
      this.$store.dispatch('history/getHistories', {isMore: false})
    },
    moveHisotryTab (label) {
      this.$store.commit('history/setLabel', {data: label})
      this.$store.dispatch('history/getHistories', {isMore: false})
    },
    compare () {
      if (this.checkedQueries.length === 2) {
        const path = `/diff/?datasource=${this.datasource}&engine=${this.engine}&queryid1=${this.checkedQueries[0]}&queryid2=${this.checkedQueries[1]}`
        window.open(path, '_blank')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.table-fixed td pre {
  white-space: pre-wrap;
}
.custom-control-label {
  font-size: 0.75rem;
  vertical-align: baseline;
  &:before {
    left: -2.0rem;
  }
  &:after {
    left: calc(-2.0rem + 2px);
  }
}
</style>

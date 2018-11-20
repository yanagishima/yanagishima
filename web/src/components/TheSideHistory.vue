<template>
  <aside id="sub">
    <div class="card">
      <div class="card-header sticky-top">
        <div class="d-flex justify-content-between align-items-center">
          <strong>Latest history</strong>
          <a href="#" @click.prevent="close"><i class="fa fa-times"></i></a>
        </div>
      </div>
      <div class="list-group list-group-flush">
        <template v-for="h in history">
          <a class="list-group-item" :class="{active: queryid === h[0]}" :key="h[0]"
             :href="buildUrl({datasource, engine, tab: 'result', queryid: h[0]})" :title="h[1]">
            <button class="btn btn-sm btn-secondary panel px-2" @click.stop.prevent="openBottomPanel(h[0])"><i
              class="fa fa-fw fa-columns fa-rotate-270"></i></button>
            <div class="ace-font word-break">
              {{h[1].compact().truncate(128)}}
            </div>
          </a>
        </template>
      </div>
    </div>
  </aside>
</template>

<script>
import {mapState} from 'vuex'
import util from '@/mixins/util'

export default {
  name: 'TheSideHistory',
  mixins: [util],
  computed: {
    ...mapState({
      datasource: state => state.hash.datasource,
      engine: state => state.hash.engine,
      queryid: state => state.hash.queryid
    }),
    ...mapState('history', {
      history: state => state.response && state.response.results ? state.response.results : []
    })
  },
  methods: {
    close () {
      this.$store.commit('setIsSideHistoryOpen', false)
    },
    openBottomPanel (queryid) {
      this.$store.dispatch('history/loadPanelQuery', {queryid})
    }
  }
}
</script>

<style scoped>
</style>

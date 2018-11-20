<template>
  <div v-if="isBottomPanelOpen" id="panel">
    <div id="panel-header" class="sticky-top" :class="`datasource_${datasourceIndex}-bgcolor`"></div>
    <div v-if="panelResult" id="panel-body" :class="isWide ? 'container-fluid' : 'container'">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
          <span v-if="panelResult.lineNumber" class="mr-3">
            <i class="fa fa-file-text-o" title="query ID" data-toggle="tooltip" data-animation="false" data-placement="left"></i>
            <strong>{{panelQueryid}}</strong>
          </span>
          <span v-if="panelResult.finishedTime" class="mr-2">
            <i class="fa fa-clock-o" title="Finished time" data-toggle="tooltip" data-animation="false" data-placement="left"></i>
            {{panelResult.finishedTime | extractDate}}
          </span>
          <span v-if="panelResult.elapsedTimeMillis" class="mr-2">
            <strong>{{(panelResult.elapsedTimeMillis / 1000).ceil(2)}}</strong><span class="text-muted ml-1">sec</span>
          </span>
          <span v-if="panelResult.rawDataSize" class="mr-2">
            <strong>{{panelResult.rawDataSize.remove('B')}}</strong><span class="text-muted ml-1">byte</span>
          </span>
          <span v-if="panelResult.lineNumber" class="mr-2">
            <strong>{{panelResult.results.length | formatNumber}}</strong>
            <template v-if="panelResult.results.length !== panelResult.lineNumber -1">
              <span class="mx-1">/</span>
              <strong>{{panelResult.lineNumber - 1 | formatNumber}}</strong>
            </template>
            <span class="text-muted ml-1">results</span>
          </span>
        </div>
        <a href="#" @click.prevent="resetPanelQueryid">
          <i class="fa fa-fw fa-times"></i>
        </a>
      </div>

      <ResultTable :result="panelResult" :pretty="isPretty" :line="line" @line-click="toggleLine"></ResultTable>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import ResultTable from '@/components/ResultTable'

export default {
  name: 'TheBottomPanel',
  components: {ResultTable},
  computed: {
    ...mapState({
      isWide: state => state.settings.isWide,
      line: state => state.hash.line
    }),
    ...mapGetters([
      'datasourceIndex',
      'isBottomPanelOpen'
    ]),
    ...mapState('history', [
      'panelQueryid',
      'panelResult'
    ]),
    ...mapState('result', [
      'isPretty'
    ])
  },
  methods: {
    toggleLine (line) {
      if (line === this.line) {
        line = 0
      }
      this.$store.commit('setHashItem', {line})
    },
    resetPanelQueryid () {
      this.$store.commit('history/setPanelQueryid', {data: null})
    }
  }
}
</script>

<style scoped>
</style>

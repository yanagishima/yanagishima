<template>
  <div id="help" class="modal fade">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title"><strong>Help</strong> - {{engine}}</h5>
          <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>
        </div>
        <div class="modal-body p-4">
          <section class="mb-5">
            <h4 class="font-weight-bold mb-3"><i class="far fa-fw fa-keyboard mr-1"></i>HotKeys</h4>
            <table class="table table-fixed">
              <thead>
              <tr>
                <th width="25%">Effective Range</th>
                <th width="25%">Keyboard</th>
                <th width="50%">Command</th>
              </tr>
              </thead>
              <tbody>
              <tr>
                <td>Screen</td>
                <td><kbd>Ctrl</kbd> + <kbd>T</kbd></td>
                <td>Move to <strong>Treeview</strong></td>
              </tr>
              <tr v-if="isPresto">
                <td>Editor</td>
                <td><kbd>Ctrl</kbd> + <kbd>Space</kbd></td>
                <td><strong>Auto-complete</strong> Table/Function</td>
              </tr>
              <tr>
                <td>Editor</td>
                <td><kbd>Ctrl</kbd> + <kbd>Enter</kbd></td>
                <td><strong>Run</strong> Query</td>
              </tr>
              <tr v-if="isPresto">
                <td>Editor</td>
                <td><kbd>Shift</kbd> + <kbd>Enter</kbd></td>
                <td><strong>Validate</strong> Query</td>
              </tr>
              <tr v-if="isPresto">
                <td>Editor</td>
                <td><kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd></td>
                <td><strong>Format</strong> Query</td>
              </tr>
              </tbody>
            </table>
          </section>
          <section class="mb-5">
            <h4 class="font-weight-bold mb-3"><i class="fas fa-fw fa-dollar-sign mr-1"></i>Variable expansion</h4>
            <p class="px-3">You can use <strong>variables</strong> as <strong>${var}</strong> in query.</p>
            <table class="table table-fixed">
              <tbody>
              <tr>
                <td width="70%">
                  <BaseAce :code="demo.variables" :readonly="true" css-class="bg-transparent"></BaseAce>
                </td>
                <td width="30%" class="text-right">
                  <a href="#" class="btn btn-sm btn-secondary" @click.prevent="variablesDemo"><i
                    class="fa fa-fw fa-play mr-1"></i>Demo</a>
                </td>
              </tr>
              </tbody>
            </table>
          </section>
          <section>
            <h4 class="font-weight-bold mb-3"><i class="far fa-fw fa-chart-bar mr-1"></i>Chart Function</h4>
            <table class="table table-fixed">
              <tbody>
              <tr>
                <td width="70%">
                  <BaseAce code="SELECT dt, pageview FROM sample" :readonly="true" css-class="bg-transparent"></BaseAce>
                </td>
                <td width="30%" class="text-right">
                  <a href="#" class="btn btn-sm btn-secondary" @click.prevent="chartDemo"><i
                    class="fa fa-fw fa-play mr-1"></i>Demo</a>
                </td>
              </tr>
              </tbody>
            </table>
          </section>
        </div>
        <div class="modal-footer">
          <a v-if="links.mailAdmin" :href="`mailto:${links.mailAdmin}`" class="btn btn-primary"><i
            class="fa fa-fw fa-envelope-o mr-1"></i>Mail to Admin</a>
          <a v-if="links.aboutThis" :href="links.aboutThis" class="btn btn-primary" target="wiki"><i
            class="fa fa-fw fa-book mr-1"></i>About yanagishima</a>
          <a v-if="links.bugsFeedback" :href="links.bugsFeedback" class="btn btn-primary" target="bug"><i
            class="fa fa-fw fa-bug mr-1"></i>Bugs and Feedback</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import util from '@/mixins/util'
import {DATE_COLUMN_NAMES, LINKS} from '@/constants'

export default {
  name: 'ModalHelp',
  mixins: [util],
  data () {
    return {
      demo: {
        /* eslint-disable no-template-curly-in-string */
        variables: 'SELECT ${x} FROM ${y} LIMIT ${z}',
        chart: 'SELECT * FROM (VALUES(2013,1000,400),(2014,1170,460),(2015,660,1120),(2016,1030,540),(2017,1220,890)) AS t (year,A,B)'
      },
      dateColumnNames: DATE_COLUMN_NAMES,
      links: LINKS
    }
  },
  computed: {
    ...mapState({
      engine: state => state.hash.engine
    }),
    ...mapGetters([
      'isPresto'
    ])
  },
  methods: {
    variablesDemo () {
      this.$store.commit('editor/setInputQuery', {data: this.demo.variables})
      this.hideModal('help')
    },
    chartDemo () {
      this.$store.commit('editor/setInputQuery', {data: this.demo.chart})
      this.$store.dispatch('result/runQuery')
      this.hideModal('help')
      this.$store.commit('setHashItem', {tab: 'result'})
      // chart = 2
    }
  }
}
</script>

<style scoped>
</style>

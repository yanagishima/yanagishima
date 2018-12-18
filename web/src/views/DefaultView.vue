<template>
  <div id="wrapper" :class="{open: isSideHistoryOpen, [`datasource_${datasourceIndex}`]: true}">
    <main id="main">
      <TheHeader @logo-click="init(true)"/>
      <TheContent v-if="!unload"/>
      <TheFooter/>

      <!-- sub windows -->
      <TheComment/>
      <TheBottomPanel/>

      <!-- modals -->
      <ModalHelp/>
      <ModalTheme/>
      <ModalPartition/>
      <ModalAuth/>
    </main>

    <!-- sub -->
    <button id="btn-panel" class="btn btn-secondary px-0 py-2" @click.prevent="openPanel" v-if="!isSideHistoryOpen"
            title="Latest history">
      <i class="fa fa-fw fa-caret-right text-primary"></i>
    </button>
    <TheSideHistory/>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import $ from 'jquery'
import toastr from 'toastr'
import Favico from 'favico.js'
import TheHeader from '@/components/TheHeader'
import TheContent from '@/components/TheContent'
import TheFooter from '@/components/TheFooter'
import ModalHelp from '@/components/modals/ModalHelp'
import ModalTheme from '@/components/modals/ModalTheme'
import ModalPartition from '@/components/modals/ModalPartition'
import ModalAuth from '@/components/modals/ModalAuth'
import TheBottomPanel from '@/components/TheBottomPanel'
import TheComment from '@/components/TheComment'
import TheSideHistory from '@/components/TheSideHistory'
import util from '@/mixins/util'
import {SITENAME, TABS} from '@/constants'

const favico = new Favico()

export default {
  name: 'DefaultView',
  components: {
    TheHeader,
    TheContent,
    TheFooter,
    ModalHelp,
    ModalTheme,
    ModalPartition,
    ModalAuth,
    TheBottomPanel,
    TheComment,
    TheSideHistory
  },
  mixins: [util],
  computed: {
    ...mapState([
      'datasources',
      'engines',
      'auths',
      'authUser',
      'authPass',
      'isModal',
      'isSideHistoryOpen',
      'unload'
    ]),
    ...mapState({
      isLocalStorage: state => state.settings.isLocalStorage,
      theme: state => state.settings.theme,
      datasource: state => state.hash.datasource,
      engine: state => state.hash.engine,
      queryid: state => state.hash.queryid,
      tab: state => state.hash.tab,
      bookmark_id: state => state.hash.bookmark_id
    }),
    ...mapGetters([
      'hashString',
      'datasourceIndex',
      'datasourceEngine'
    ]),
    ...mapState('result', {
      runningQueries: state => state.runningQueries,
      loadingResult: state => state.loading
    })
  },
  watch: {
    datasources (val) {
      // run once at first
      if (!val.length) {
        location.replace('/error/?403')
      }

      this.loadLocalStorageGlobal()

      this.$store.commit('setHash')

      const datasource = this.datasource || val[0]
      const engine = this.engine || this.engines[datasource][0]
      const tab = this.tab || TABS[0].id
      this.$store.commit('setHashItem', {datasource, engine, tab})

      this.loadLocalStoragePerDatasource()
      this.$store.commit('loadComplete')
    },
    hashString () {
      location.hash = this.hashString
      this.setTitle()
    },
    datasourceEngine (val, old) {
      if (old) {
        this.$store.commit('setHashItem', {queryid: '', bookmark_id: ''})
        this.$store.commit('editor/setInputQuery', {data: ''})
        this.$store.commit('setHashItem', {tab: TABS[0].id})
      }

      const [ds] = val.split('_')
      const [oldDs] = old.split('_')
      if (ds !== oldDs) {
        // datasource changed
        if (!this.engines[ds].includes(this.engine)) {
          this.$store.commit('setHashItem', {engine: this.engines[ds][0]})
        }
        this.checkAuth()
        this.loadLocalStoragePerDatasource()
      }

      this.init()
      this.$store.dispatch('editor/getCompleteWords')
    },
    queryid (val, old) {
      if (old) {
        this.$store.commit('setHashItem', {line: 0})
      }
      if (val) {
        this.$store.dispatch('result/loadQuery')
      }
    },
    bookmark_id (val) {
      this.$store.dispatch('bookmark/getBookmark', {bookmarkId: val})
    },
    tab () {
      $(document).scrollTop(0)
    },
    isLocalStorage () {
      this.init()
    },
    runningQueries (val) {
      favico.badge(val)
      this.$store.dispatch('history/getHistories', {isMore: false})
    },
    unload () {
      this.$store.commit('editor/focusOnEditor')
    }
  },
  created () {
    window.addEventListener('hashchange', () => {
      this.$store.commit('setHash')
    })
    this.$store.dispatch('getDataSources')
  },
  mounted () {
    const self = this

    $('body').tooltip({selector: '[data-toggle="tooltip"]'})

    $(document).on('shown.bs.modal', '.modal', function () {
      self.$store.commit('setIsModal', true)
      self.$store.commit('editor/resetFocusOnEditor')
    }).on('hidden.bs.modal', function () {
      self.$store.commit('setIsModal', false)
      self.$store.commit('editor/focusOnEditor')
      $(document).scrollTop(0)
    })

    $(document).on('show.bs.modal', '#bookmark', function () {
      $('button').blur()
    }).on('hide.bs.modal', function () {
    })

    // Hotkey
    $(window).keydown(function (e) {
      if (e.ctrlKey) {
        if (e.keyCode === 84) { // T
          self.$store.commit('setHashItem', {tab: 'treeview'})
          e.preventDefault()
        }
      }
      if (self.isModal) {
        e.keyCode === 27 && $('.modal').modal('hide') // ESC
      }
    })

    // detect loading job
    $(window).on('beforeunload', function () {
      if (self.loadingResult) {
        return confirm('Do you want to do it?')
      }
    })

    // hidden command
    const configs = {
      konami: [38, 38, 40, 40, 37, 39, 37, 39, 66, 65]
    }
    const maxCommandLength = Math.max(...Object.values(configs).map(a => a.length))
    let inputs = []
    $(window).keyup(function (e) {
      inputs.push(e.keyCode)
      inputs.splice(0, inputs.length - maxCommandLength)
      if (inputs.toString().indexOf(configs.konami) >= 0) {
        self.superadminMode = true
        toastr.success('You can watch all queries.', 'Super Admin Mode')
        inputs = []
      }
    })
  },
  methods: {
    init (all) {
      if (all) {
        this.$store.commit('init')
        this.$store.commit('editor/init')
      }

      this.$store.commit('treeview/init')
      this.$store.commit('result/init')
      this.$store.commit('history/init')
      this.$store.commit('bookmark/init')
      this.$store.commit('qlist/init')
      this.$store.commit('timeline/init')

      this.loadLocalStoragePerDatasource()

      this.$store.dispatch('history/getHistories', {isMore: false, filter: ''})
      this.$store.dispatch('bookmark/getBookmarks')
      this.$store.dispatch('qlist/getQlist', {isAutoQlist: false})

      this.setTitle()
    },
    loadLocalStorageGlobal () {
      this.$store.commit('loadLocalStorage')
      this.$store.commit('qlist/loadLocalStorage')
    },
    loadLocalStoragePerDatasource () {
      this.$store.commit('bookmark/loadLocalStorage', {datasource: this.datasource})
      this.$store.commit('history/loadLocalStorage', {datasource: this.datasource})
    },
    checkAuth () {
      if (!this.auths[this.datasource]) {
        return
      }

      this.$store.commit('loadAuth')

      if (!this.authUser || !this.authPass) {
        this.showModal('auth')
      }
    },
    setTitle (val) {
      const subTitle = val || this.datasource
      const tab = TABS.find(t => t.id === this.tab)
      let pageTitle = tab.name
      if (this.tab === 'result' && this.queryid) {
        pageTitle = `#${this.queryid}`
      }
      document.title = `[${subTitle}] ${pageTitle} - ${SITENAME}`
    },
    openPanel () {
      this.$store.commit('setIsSideHistoryOpen', true)
    }
  }
}
</script>

<style scoped>
</style>

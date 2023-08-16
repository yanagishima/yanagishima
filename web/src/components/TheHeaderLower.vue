<template>
  <div id="header-lower">
    <div class="editor mb-2">
      <BaseAce :code="inputQuery" :goto-line="gotoLine" :focus="focus" :min-lines="settings.minline" :max-lines="Infinity"
           :theme="settings.theme" @change-code="setInputQuery" @run-code="runQuery"
           :error-line="errorLine" :error-text="tinyErrorText" :readonly="loading"
           :complete-words="(isPresto || isTrino) ? completeWords : []"
           @format-code="(isPresto || isTrino) ? formatQuery() : () => {}" @validate-code="(isPresto || isTrino) ? validateQuery() : () => {}"></BaseAce>
    </div>
    <fieldset v-if="variables.length" id="variables" class="mb-3">
      <legend>{{variables.length}} variables</legend>
      <div class="form-inline">
        <template v-for="v in variables">
          <label class="mr-2" :key="`label_${v.key}`">{{v.key}}</label>
          <input type="text" :id="`variable_${v.key}`" v-model="v.value" :key="`input_${v.key}`"
                 class="form-control form-control-sm mr-3" size="10" autocomplete="off">
        </template>
      </div>
    </fieldset>
    <div class="row align-items-end">
      <div class="col-7 col-lg-6">
        <ul class="nav nav-tabs">
          <li class="nav-item" v-for="t in tabs" :key="t.id">
            <a class="nav-link" href="#" @click.prevent="setTab(t.id)" :class="{active: t.id === tab}">
              <i class="fa-fw mr-1" :class="`${t.iconStyle || 'fas'} fa-${t.icon}`"></i><span class="d-none d-xl-inline">{{t.name}}</span>
            </a>
          </li>
        </ul>
      </div>
      <div class="col text-right pb-2">
        <div id="control" class="d-inline-block">
          <div class="btn-group ml-3">
            <button type="button" class="btn btn-secondary px-2" @click.prevent="convertQuery"
                    data-toggle="tooltip" data-animation="false" title="Convert Query"
                    :disabled="!inputQuery.length || loading"><i class="fa fa-fw fa-exchange"></i>
            </button>
            <button v-if="isPresto || isTrino" type="button" class="btn btn-secondary px-2" @click.prevent="formatQuery"
                    data-toggle="tooltip" data-animation="false" title="Format Query"
                    :disabled="!inputQuery.length || loading"><i class="fa fa-fw fa-indent"></i>
            </button>
            <button type="button" class="btn btn-secondary px-2" v-clipboard="inputQuery"
                    data-toggle="tooltip" data-animation="false" title="Copy to Clipboard"
                    :disabled="!inputQuery.length || loading"><i class="fa fa-fw fa-clipboard"></i>
            </button>
            <button type="button" class="btn btn-secondary px-2" @click.prevent="addBookmarkItem"
                    data-toggle="tooltip" data-animation="false" title="Add to Bookmarks"
                    :disabled="!inputQuery.length || existBookmark"><i class="far fa-fw fa-star"></i></button>
          </div>
          <div class="btn-group ml-3">
            <button type="button" class="btn btn-primary" :disabled="!runnable"
                  @click="runQuery()"><i class="fa fa-fw fa-play mr-1"></i><strong>Run</strong>
            </button>
            <button type="button" class="btn btn-primary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" :disabled="!runnable">
              <span class="sr-only">Toggle Dropdown</span>
            </button>
            <div class="dropdown-menu dropdown-menu-right" style="right: auto">
              <template v-if="isPresto">
                <button type="button" class="dropdown-item" :disabled="!runnable" @click="explainQuery">Explain (Text)</button>
                <button type="button" class="dropdown-item" :disabled="!runnable" @click="explainGraphvizQuery">Explain (Graph)</button>
              </template>
              <template v-else-if="isTrino || isHive || isSpark">
                <button type="button" class="dropdown-item" :disabled="!runnable" @click="explainQuery">Explain</button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import {TABS} from '@/constants'

export default {
  name: 'TheHeaderLower',
  data () {
    return {
      tabs: TABS
    }
  },
  computed: {
    ...mapState([
      'settings'
    ]),
    ...mapState({
      tab: state => state.hash.tab,
      engine: state => state.hash.engine,
      bookmark_params: state => state.hash.bookmark_params
    }),
    ...mapState('editor', [
      'inputQuery',
      'gotoLine',
      'focus',
      'errorLine',
      'errorText',
      'completeWords'
    ]),
    ...mapState('bookmark', {
      bookmarks: state => state.response,
      addedBookmarkId: state => state.addedBookmarkId
    }),
    ...mapGetters([
      'isPresto',
      'isHive',
      'isSpark',
      'isTrino',
      'datasourceIndex',
      'datasourceEngine'
    ]),
    ...mapState('result', [
      'loading'
    ]),
    variables () {
      const detectedVariables = this.inputQuery.match(/\${[a-zA-Z]([a-zA-Z0-9_]+)?}/g)
      if (detectedVariables === null) {
        return []
      }
      try {
        const o = JSON.parse(this.bookmark_params)
        return detectedVariables.unique().map(v => ({str: v, key: v.remove(/[${}]/g), value: o[v.remove(/[${}]/g)]}))
      } catch (e) {
        return detectedVariables.unique().map(v => ({str: v, key: v.remove(/[${}]/g), value: ''}))
      }
    },
    existBookmark () {
      return this.bookmarks.some(b => b.query === this.inputQuery)
    },
    tinyErrorText () {
      const text = this.errorText
      if (text) {
        return text.remove(/^Query failed \(#[0-9a-z_]+\): /).remove(/^line [0-9]+:[0-9]+: /).truncate(192)
      } else {
        return text
      }
    },
    runnable () {
      return this.inputQuery && !this.loading && this.datasourceIndex
    }
  },
  watch: {
    inputQuery () {
      this.$store.commit('editor/resetError')
      if (this.addedBookmarkId) {
        this.$store.commit('bookmark/resetAddedBookmarkId')
      }
    }
  },
  methods: {
    setInputQuery (query) {
      this.$store.commit('editor/setInputQuery', {data: query})
    },
    formatQuery () {
      this.$store.dispatch('editor/formatQuery')
      this.$store.commit('editor/focusOnEditor')
    },
    validateQuery () {
      this.$store.dispatch('editor/validateQuery')
    },
    convertQuery () {
      this.$store.dispatch('editor/convertQuery')
    },
    runQuery (query, translateFlag) {
      if (this.loading || !this.datasourceIndex) {
        return false
      }

      query = query || this.inputQuery

      // variables expansion
      if (this.variables.length) {
        const errorVariables = []
        for (const v of this.variables) {
          const pattern = new RegExp(RegExp.escape(v.str), 'g')
          if (v.value.length) {
            query = query.replace(pattern, v.value)
          } else {
            errorVariables.push(v.key)
          }
        }
        if (errorVariables.length) {
          alert('Input to variables ' + errorVariables.join(', '))
          return false
        }
        this.setInputQuery(query)
      }

      this.$store.dispatch('result/runQuery', {query, translateFlag})

      this.setTab('result')
    },
    explainQuery () {
      this.runQuery(`EXPLAIN ${this.inputQuery}`)
    },
    explainGraphvizQuery () {
      const engine = this.engine
      if (engine === 'presto') {
        this.runQuery(`EXPLAIN (FORMAT GRAPHVIZ) ${this.inputQuery}`)
      } else {
        throw new Error('not supported')
      }
    },
    addBookmarkItem () {
      this.$store.dispatch('bookmark/addBookmarkItem')
    },
    setTab (tab) {
      this.$store.commit('setHashItem', {tab})
    }
  }
}
</script>

<style scoped>
</style>

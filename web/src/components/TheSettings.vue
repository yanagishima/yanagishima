<template>
  <div id="header-setting">
    <div class="header-close">
      <button class="close text-white" @click.prevent="closeSetting"><span>&times;</span></button>
    </div>
    <div class="container">
      <div class="row">
        <div class="col-lg-3 mb-3 mb-lg-0">
          <dl class="card card-inverse mb-0">
            <dt class="card-header">General</dt>
            <dd class="card-body mb-0">
              <ul class="list-unstyled mb-0">
                <li title="Wide Screen">
                  <label :class="{'text-white': isWide}">
                    <input type="checkbox" class="align-middle mr-1" v-model="isWide">
                    Wide Screen
                  </label>
                </li>
                <li title="Automatically Refresh Query List">
                  <label :class="{'text-white': isAutoQlist}">
                    <input type="checkbox" class="align-middle mr-1" v-model="isAutoQlist">
                    Automatically Refresh Query List
                  </label>
                </li>
                <li v-if="isHttps()" title="Desktop Notification (when in background)">
                  <label :class="{'text-white': desktopNotification}">
                    <input type="checkbox" class="align-middle mr-1" v-model="desktopNotification">
                    Desktop Notification (when in background)
                  </label>
                </li>
                <li v-if="datasources.length > 1" title="Remember Last Datasource">
                  <label :class="{'text-white': rememberDatasource}">
                    <input type="checkbox" class="align-middle mr-1" v-model="rememberDatasource">
                    Remember Last Datasource
                  </label>
                </li>
                <li v-if="engines[datasource] && engines[datasource].length > 1" title="Remember Last Engine">
                  <label :class="{'text-white': rememberEngine}">
                    <input type="checkbox" class="mr-1" v-model="rememberEngine">
                    Remember Last Engine
                  </label>
                </li>
              </ul>
            </dd>
          </dl>
        </div>
        <div class="col-lg-3 mb-3 mb-lg-0">
          <dl class="card card-inverse mb-0 h-100">
            <dt class="card-header">Editor</dt>
            <dd class="card-body mb-0">
              <ul class="list-unstyled mb-0">
                <li class="row mb-2">
                  <div class="col-3 pr-0 text-truncate" title="Minline">Minline</div>
                  <div class="col-9 text-right">
                    <div class="btn-group">
                      <a class="btn btn-sm btn-secondary" href="#" v-for="item in minlines" :key="item"
                        @click.prevent="minline = item" :class="{'selected': item === minline}">
                        <template>{{item}}</template>
                      </a>
                    </div>
                  </div>
                </li>
                <li class="row mb-2">
                  <div class="col-5" title="Theme">Theme</div>
                  <div class="col-7 text-right">
                    <button class="btn btn-sm btn-secondary selected dropdown-toggle" @click="showModal('theme')">
                      {{theme | humanize}}
                    </button>
                  </div>
                </li>
              </ul>
            </dd>
          </dl>
        </div>
        <div class="col-lg-3 mb-3 mb-lg-0">
          <dl class="card card-inverse mb-0 h-100">
            <dt class="card-header">Download</dt>
            <dd class="card-body mb-0">
              <ul class="list-unstyled mb-0">
                <li class="row mb-2">
                  <div class="col-5" title="Format">Format</div>
                  <div class="col-7 text-right">
                    <div class="btn-group">
                      <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isCsv = false"
                        :class="{'selected': !isCsv}">TSV</a>
                      <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isCsv = true"
                        :class="{'selected': isCsv}">CSV</a>
                    </div>
                  </div>
                </li>
                <li class="row mb-2">
                  <div class="col-5" title="Include header">Include header</div>
                  <div class="col-7 text-right">
                    <div class="btn-group">
                      <a class="btn btn-sm btn-secondary" href="#" @click.prevent="includeHeader = true"
                        :class="{'selected': includeHeader}">Yes</a>
                      <a class="btn btn-sm btn-secondary" href="#" @click.prevent="includeHeader = false"
                        :class="{'selected': !includeHeader}">No</a>
                    </div>
                  </div>
                </li>
              </ul>
            </dd>
          </dl>
        </div>
        <div class="col-lg-3">
          <dl class="card card-inverse mb-0 h-100">
            <dt class="card-header row mx-0">
              <div class="col px-0 text-truncate">Storage</div>
              <div class="col px-0 text-right">
                <div class="btn-group">
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isLocalStorage = 0"
                     :class="{'selected': !isLocalStorage}">Server</a>
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isLocalStorage = 1"
                     :class="{'selected': isLocalStorage}">Browser</a>
                </div>
              </div>
            </dt>
            <dd class="card-body mb-0">
              <ul class="list-unstyled mb-0">
                <li class="row mb-2">
                  <div class="col-6 text-truncate" :title="`History (${datasource})`">History<span class="ml-1" v-if="datasources.length > 1">({{datasource}})</span></div>
                  <div class="col-6 pl-0 text-right">
                    <span class="mr-1"><strong>{{history.length}}</strong> items</span>
                    <div class="btn-group">
                      <button class="btn btn-sm btn-secondary selected dropdown-toggle" data-toggle="dropdown"
                              :disabled="!isLocalStorage">Action
                      </button>
                      <div class="dropdown-menu dropdown-menu-right">
                        <a class="dropdown-item" href="#" @click.prevent="exportHistory">Export</a>
                        <a class="dropdown-item" href="#" @click.prevent="importHistory">Import</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item text-danger" href="#" @click.prevent="deleteAllHistory">Remove</a>
                      </div>
                    </div>
                  </div>
                </li>
                <li class="row mb-2">
                  <div class="col-6 text-truncate" :title="`Bookmark (${datasource})`">Bookmark<span class="ml-1" v-if="datasources.length > 1">({{datasource}})</span></div>
                  <div class="col-6 pl-0 text-right">
                    <span class="mr-1"><strong>{{bookmarks.length}}</strong> items</span>
                    <div class="btn-group">
                      <button class="btn btn-sm btn-secondary selected dropdown-toggle" data-toggle="dropdown"
                              :disabled="!isLocalStorage">Action
                      </button>
                      <div class="dropdown-menu dropdown-menu-right">
                        <a class="dropdown-item" href="#" @click.prevent="exportBookmark">Export</a>
                        <a class="dropdown-item" href="#" @click.prevent="importBookmark">Import</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item text-danger" href="#" @click.prevent="deleteAllBookmark">Remove</a>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
            </dd>
          </dl>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState} from 'vuex'
import util from '@/mixins/util'

function makeSettingComputed (key) {
  return {
    get () {
      return this.settings[key]
    },
    set (val) {
      this.$store.commit('setSettings', {[key]: val})
    }
  }
}

export default {
  name: 'TheSettings',
  mixins: [util],
  data () {
    return {
      minlines: [
        2,
        4,
        8,
        16
      ]
    }
  },
  computed: {
    ...mapState([
      'settings',
      'datasources',
      'engines'
    ]),
    ...mapState({
      datasource: state => state.hash.datasource
    }),
    ...mapState('editor', [
      'inputQuery'
    ]),
    ...mapState('history', {
      historyIds: state => state.historyIds,
      history: state => state.response && state.response.results ? state.response.results : []
    }),
    ...mapState('bookmark', [
      'bookmarks'
    ]),
    isWide: makeSettingComputed('isWide'),
    isAutoQlist: makeSettingComputed('isAutoQlist'),
    desktopNotification: makeSettingComputed('desktopNotification'),
    rememberDatasource: makeSettingComputed('rememberDatasource'),
    rememberEngine: makeSettingComputed('rememberEngine'),
    minline: makeSettingComputed('minline'),
    theme: makeSettingComputed('theme'),
    isCsv: makeSettingComputed('isCsv'),
    includeHeader: makeSettingComputed('includeHeader'),
    isLocalStorage: makeSettingComputed('isLocalStorage')
  },
  methods: {
    isHttps () {
      return location.protocol === 'https:'
    },
    exportHistory () {
      this.$store.commit('editor/setInputQuery', {data: this.historyIds.join(',')})
    },
    importHistory () {
      if (!this.inputQuery.length) {
        alert('Editor is empty')
        return false
      }
      if (confirm('Do you want to overwrite it?')) {
        this.$store.dispatch('history/importHistory', {data: this.inputQuery})
      }
    },
    deleteAllHistory () {
      if (confirm('Do you want to remove it?')) {
        this.$store.dispatch('history/deleteAllLocalHistory')
      }
    },
    exportBookmark () {
      this.$store.commit('editor/setInputQuery', {data: this.bookmarks.join(',')})
    },
    importBookmark () {
      if (!this.inputQuery.length) {
        alert('Editor is empty.')
        return false
      }
      if (confirm('Do you want to overwrite it?')) {
        this.$store.dispatch('bookmark/importBookmarks', {data: this.inputQuery})
      }
    },
    deleteAllBookmark () {
      if (confirm('Do you want to remove it?')) {
        this.$store.dispatch('bookmark/deleteAllLocalBookmarks')
      }
    },
    closeSetting () {
      this.$store.commit('setIsSettingOpen', false)
    }
  }
}
</script>

<style scoped>
</style>

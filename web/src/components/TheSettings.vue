<template>
  <div id="header-setting">
    <div class="container">
      <div class="card-deck">
        <div class="card card-inverse">
          <div class="card-header">General</div>
          <div class="card-block pb-1">
            <div>
              <label :class="{'text-white': isWide}">
                <input type="checkbox" class="mr-1" v-model="isWide">
                Wide Screen
              </label>
            </div>
            <div>
              <label :class="{'text-white': isAutoQlist}">
                <input type="checkbox" class="mr-1" v-model="isAutoQlist">
                Automatically Refresh Query List
              </label>
            </div>
            <div v-if="isHttps()">
              <label :class="{'text-white': desktopNotification}">
                <input type="checkbox" class="mr-1" v-model="desktopNotification">
                Desktop Notification (when in background)
              </label>
            </div>
            <div v-if="datasources.length > 1">
              <label :class="{'text-white': rememberDatasource}">
                <input type="checkbox" class="mr-1" v-model="rememberDatasource">
                Remember Last Datasource
              </label>
            </div>
            <div v-if="engines[datasource] && engines[datasource].length > 1">
              <label :class="{'text-white': rememberEngine}">
                <input type="checkbox" class="mr-1" v-model="rememberEngine">
                Remember Last Engine
              </label>
            </div>
          </div>
        </div>
        <div class="card card-inverse">
          <div class="card-header">Editor</div>
          <div class="card-block pb-1">
            <div class="row mb-2">
              <div class="col-3">Minline</div>
              <div class="col-9">
                <div class="btn-group">
                  <a class="btn btn-sm btn-secondary" href="#" v-for="item in minlines" :key="item"
                     @click.prevent="minline = item" :class="{'selected': item === minline}">
                    <template>{{item}}</template>
                  </a>
                </div>
              </div>
            </div>
            <div class="row mb-2">
              <div class="col-3">Theme</div>
              <div class="col-9">
                <button class="btn btn-sm btn-secondary selected dropdown-toggle" @click="showModal('theme')">
                  {{theme | humanize}}
                </button>
              </div>
            </div>
          </div>
        </div>
        <div class="card card-inverse">
          <div class="card-header">Download</div>
          <div class="card-block pb-1">
            <div class="row mb-2">
              <div class="col-6">Format</div>
              <div class="col-6 text-right">
                <div class="btn-group">
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isCsv = false"
                      :class="{'selected': !isCsv}">TSV</a>
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isCsv = true"
                      :class="{'selected': isCsv}">CSV</a>
                </div>
              </div>
            </div>
            <div class="row mb-2">
              <div class="col-6">Include header</div>
              <div class="col-6 text-right">
                <div class="btn-group">
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="includeHeader = true"
                      :class="{'selected': includeHeader}">Yes</a>
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="includeHeader = false"
                      :class="{'selected': !includeHeader}">No</a>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="card card-inverse">
          <div class="card-header">
            <div class="row">
              <div class="col-6">
                Storage
              </div>
              <div class="col-6 text-right">
                <div class="btn-group">
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isLocalStorage = 0"
                     :class="{'selected': !isLocalStorage}">Server</a>
                  <a class="btn btn-sm btn-secondary" href="#" @click.prevent="isLocalStorage = 1"
                     :class="{'selected': isLocalStorage}">Browser</a>
                </div>
              </div>
            </div>
          </div>
          <div class="card-block pb-1">
            <div class="row mb-2">
              <div class="col-4">History<span class="ml-1" v-if="datasources.length > 1">({{datasource}})</span></div>
              <div class="col-8 text-right">
                <span class="mr-2"><strong>{{history.length}}</strong> items</span>
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
            </div>
            <div class="row mb-2">
              <div class="col-4">Bookmark<span class="ml-1" v-if="datasources.length > 1">({{datasource}})</span></div>
              <div class="col-8 text-right">
                <span class="mr-2"><strong>{{bookmarks.length}}</strong> items</span>
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
            </div>
          </div>
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
    }
  }
}
</script>

<style scoped>
</style>

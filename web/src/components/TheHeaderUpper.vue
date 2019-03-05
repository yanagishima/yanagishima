<template>
  <div id="header-upper" class="py-2">
    <div class="row align-items-center">
      <div class="col-8">
        <h1 id="logo" class="d-inline-block mr-4">
          <a :href="buildUrl({datasource, engine})" @click.prevent="$emit('logo-click')">
            <span id="logo-figure" class="mr-2"></span>{{sitename}}
          </a>
          <span class="l-2 font-weight-normal">{{version}}</span>
        </h1>
        <span class="mr-2">
          <small class="mr-1">Datasource</small>
          <template v-if="datasources && datasources.length > 1">
            <div class="btn-group">
              <button v-for="item in datasources" :key="item" @click.prevent="setDatasource(item)" class="btn btn-sm"
                      :class="{'btn-primary': item !== datasource, 'btn-secondary': item === datasource}">{{item}}</button>
            </div>
          </template>
          <template v-else>
            <strong>{{datasource}}</strong>
          </template>
        </span>
        <span class="mr-2">
          <small class="mr-1">Engine</small>
          <template v-if="engines[datasource] && engines[datasource].length > 1">
            <div class="btn-group">
              <button v-for="item in engines[datasource]" :key="item" @click.prevent="setEngine(item)" class="btn btn-sm"
                      :class="{'btn-primary': item !== engine, 'btn-secondary': item === engine}">{{item}}</button>
            </div>
          </template>
          <template v-else>
            <strong>{{engine}}</strong>
          </template>
        </span>
        <span v-if="auths[datasource]" class="mr-2">
          <small class="mr-1">User</small>
          <button type="button" class="btn btn-sm btn-primary" data-toggle="modal" data-target="#auth">
            <template v-if="authUser">
              {{authUser}}
            </template>
            <template v-else>
              ?
            </template>
          </button>
        </span>
      </div>
      <div class="col-4 text-right">
        <template v-if="isPresto">
          <a href="https://prestosql.io/docs/current/" class="text-white mr-2" target="_blank"><i
            class="fa fa-fw fa-external-link mr-1"></i>Presto Doc</a>
        </template>
        <template v-else-if="isHive">
          <a href="https://cwiki.apache.org/confluence/display/Hive/LanguageManual" class="text-white mr-2"
             target="_blank"><i class="fa fa-fw fa-external-link mr-1"></i>Hive Doc</a>
        </template>
        <template v-else-if="isSpark">
          <a href="https://spark.apache.org/" class="text-white mr-2"
             target="_blank"><i class="fa fa-fw fa-external-link mr-1"></i>Spark Doc</a>
        </template>
        <a href="#help" class="text-white mr-2" data-toggle="modal" data-target="#help"><i
          class="fa fa-fw fa-question mr-1"></i>Help</a>
        <a href="#settings" class="text-white mr-2" @click.prevent="toggleSettingOpen"><i
          class="fa fa-fw fa-cog mr-1" :class="{'fa-spin': isSettingOpen}"></i>Settings</a>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import util from '@/mixins/util'
import {SITENAME, VERSION} from '@/constants'

export default {
  name: 'TheHeaderUpper',
  mixins: [util],
  data () {
    return {
      sitename: SITENAME,
      version: VERSION
    }
  },
  computed: {
    ...mapState([
      'datasources',
      'engines',
      'auths',
      'hash',
      'authUser',
      'isSettingOpen'
    ]),
    ...mapState({
      datasource: state => state.hash.datasource,
      engine: state => state.hash.engine
    }),
    ...mapGetters([
      'isPresto',
      'isHive',
      'isSpark'
    ])
  },
  methods: {
    setDatasource (datasource) {
      this.$store.commit('setHashItem', {datasource})
    },
    setEngine (engine) {
      this.$store.commit('setHashItem', {engine})
    },
    toggleSettingOpen () {
      this.$store.commit('setIsSettingOpen', !this.isSettingOpen)
    }
  }
}
</script>

<style scoped>
</style>

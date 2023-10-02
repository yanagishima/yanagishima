<template>
  <div id="header-upper" class="py-2">
    <div class="d-flex align-items-center justify-content-between">
      <div>
        <h1 id="logo" class="d-inline-block mr-4">
          <a :href="buildUrl({datasource, engine})" @click.prevent="$emit('logo-click')">
            <span id="logo-figure" class="mr-2"></span>{{sitename}}
          </a>
        </h1>
        <span class="mr-2">
          <small>Source</small>
          <template v-if="datasources && datasources.length > 1">
            <div class="dropdown d-inline-block">
              <button class="btn btn-sm btn-primary" data-toggle="dropdown">{{datasource}}</button>
              <div class="dropdown-menu">
                <button v-for="d in datasources" :key="d" class="dropdown-item" :class="{active: d === datasource}"
                        @click.prevent="setDatasource(d)">{{d}}</button>
              </div>
            </div>
          </template>
          <template v-else>
            <strong>{{datasource}}</strong>
          </template>
        </span>
        <span class="mr-2">
          <small>Engine</small>
          <template v-if="engines[datasource] && engines[datasource].length > 1">
            <div class="dropdown d-inline-block">
              <button class="btn btn-sm btn-primary" data-toggle="dropdown">{{engine}}</button>
              <div class="dropdown-menu">
                <button v-for="e in engines[datasource]" :key="e" class="dropdown-item" :class="{active: e === engine}"
                        @click="setEngine(e)">{{e}}</button>
              </div>
            </div>
          </template>
          <template v-else>
            <strong>{{engine}}</strong>
          </template>
        </span>
      </div>
      <div>
        <a href="#notification" class="text-white mr-2" data-toggle="modal" data-target="#notification">
          <span class="notification-bell-wrapper" :class="{unread: hasNotificationUnread}"><i class="fas fa-lg fa-bell notification-bell"></i></span>
        </a>
        <template v-if="auths[datasource]">
          <a href="#auth" class="text-white mr-2" data-toggle="modal" data-target="#auth">
            <i v-if="authUser" class="fas fa-lg fa-user"></i>
            <i v-else class="fas fa-lg fa-user-times"></i>
          </a>
        </template>
        <div class="dropdown d-inline-block mr-2">
          <a href="#" data-toggle="dropdown" class="text-white"><i class="fa fa-lg fa-question"></i></a>
          <div class="dropdown-menu dropdown-menu-right" style="right: auto">
            <small class="dropdown-item-text text-muted ml-2">Version {{version}}</small>
            <div class="dropdown-divider my-1"></div>
            <a href="#help" class="dropdown-item mr-2" data-toggle="modal" data-target="#help">Help</a>
            <a v-if="isPresto" href="https://prestodb.io/docs/current/" class="dropdown-item" target="_blank" rel="noopener">Presto Doc</a>
            <a v-if="isTrino" href="https://trino.io/docs/current/" class="dropdown-item" target="_blank" rel="noopener">Trino Doc</a>
            <a v-if="isHive" href="https://cwiki.apache.org/confluence/display/Hive/LanguageManual" class="dropdown-item" target="_blank" rel="noopener">Hive Doc</a>
            <a v-if="isSpark" href="https://spark.apache.org/" class="dropdown-item" target="_blank" rel="noopener">Spark Doc</a>
          </div>
        </div>
        <a href="#settings" class="text-white" @click.prevent="toggleSettingOpen"><i
          class="fa fa-lg fa-cog" :class="{'fa-spin': isSettingOpen}"></i></a>
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
      'isTrino',
      'isHive',
      'isSpark'
    ]),
    ...mapGetters('notification', {
      hasNotificationUnread: 'hasUnread'
    })
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

<style scoped lang="scss">
$duration: 1s;
$timing: ease-out;
$count: 5;

.notification-bell-wrapper {
  display: inline-block;
  position: relative;
  width: 1rem;
  height: 1rem;
  text-align: center;

  &.unread {
    &:before, &:after {
      content: "";
      position: absolute;
      width: 100%;
      height: 100%;
      top: 0;
      left: 0;
      border: 2px solid white;
      border-radius: 50%;
      opacity: 0;
    }
  }

  &:before {
    animation: notification-bell-before $duration $timing 0s $count;
  }

  &:after {
    animation: notification-bell-after $duration $timing 0s $count;
  }
}

.unread .notification-bell {
  animation: notification-bell-swing $duration $timing 0s $count;
}

@keyframes notification-bell-before {
  0%, 40% {
    transform: scale(0);
    opacity: 0;
  }

  50% {
    transform: scale(1.2);
    opacity: .2;
  }

  100% {
    transform: scale(2);
    opacity: 0
  }
}

@keyframes notification-bell-after {
  0%, 40% {
    transform: scale(0);
    opacity: 0;
  }

  50% {
    transform: scale(1.5);
    opacity: .1;
  }

  100% {
    transform: scale(2.3);
    opacity: 0;
  }
}

@keyframes notification-bell-swing {
  30% {
    transform: rotateZ(0);
  }

  40% {
    transform: rotateZ(-30deg);
  }

  60% {
    transform: rotateZ(25deg);
  }

  80% {
    transform: rotateZ(-15deg);
  }

  100% {
    transform: rotateZ(5deg);
  }
}
</style>

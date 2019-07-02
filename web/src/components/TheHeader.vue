<template>
  <header id="header">
    <TheSettings v-if="isSettingOpen"/>
    <div v-if="isAnnounceUnread" class="alert alert-warning d-flex justify-content-between mb-0 px-4">
      <span><i class="fas fa-exclamation-triangle mr-1"></i><BaseAutoLink :text="announce.text"></BaseAutoLink></span>
      <button class="btn btn-link alert-link p-0" @click="closeAnnounce"><u>confirm</u></button>
    </div>
    <div id="header-main" :class="`engine_${engine}`">
      <div class="container">
        <TheHeaderUpper @logo-click="$emit('logo-click')"/>
        <TheHeaderLower/>
      </div>
    </div>
  </header>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import TheSettings from '@/components/TheSettings'
import TheHeaderUpper from '@/components/TheHeaderUpper'
import TheHeaderLower from '@/components/TheHeaderLower'

export default {
  name: 'TheHeader',
  components: {
    TheSettings,
    TheHeaderUpper,
    TheHeaderLower
  },
  computed: {
    ...mapState({
      engine: state => state.hash.engine,
      isSettingOpen: state => state.isSettingOpen
    }),
    ...mapGetters([
      'datasourceEngine'
    ]),
    ...mapState('announce', [
      'announce'
    ]),
    ...mapGetters('announce', {
      isAnnounceUnread: 'isUnread'
    })
  },
  methods: {
    closeAnnounce () {
      this.$store.commit('announce/read')
    }
  }
}
</script>

<style scoped>
</style>

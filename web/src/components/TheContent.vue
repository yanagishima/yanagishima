<template>
  <div id="content" :class="{open: isBottomPanelOpen}">
    <div :class="isWide ? 'container-fluid' : 'container'">
      <TabQlist v-if="tab === 'qlist'"/>
      <TabHistory v-else-if="tab === 'history'"/>
      <TabBookmark v-else-if="tab === 'bookmark'"/>
      <TabResult v-else-if="tab === 'result'"/>
      <TabTreeview v-else-if="tab === 'treeview'"/>
      <TabTimeline v-else-if="tab === 'timeline'"/>
    </div>
  </div>
</template>

<script>
import {mapState, mapGetters} from 'vuex'
import TabQlist from '@/components/tabs/TabQlist'
import TabHistory from '@/components/tabs/TabHistory'
import TabBookmark from '@/components/tabs/TabBookmark'
import TabResult from '@/components/tabs/TabResult'
import TabTreeview from '@/components/tabs/TabTreeview'
import TabTimeline from '@/components/tabs/TabTimeline'

export default {
  name: 'TheContent',
  components: {
    TabQlist,
    TabHistory,
    TabBookmark,
    TabResult,
    TabTreeview,
    TabTimeline
  },
  computed: {
    ...mapState({
      isWide: state => state.settings.isWide,
      tab: state => state.hash.tab
    }),
    ...mapGetters([
      'isBottomPanelOpen'
    ])
  }
}
</script>

<style scoped>
</style>

<template>
  <header id="header">
    <TheSettings v-if="isSettingOpen"/>
    <div id="header-main" :class="`engine_${engine}`">
      <div class="container">
        <TheHeaderUpper @logo-click="$emit('logo-click')"/>
        <TheHeaderLower/>
      </div>
    </div>
  </header>
</template>

<script>
import {mapState} from 'vuex'
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
    })
  }
}
</script>

<style scoped>
</style>

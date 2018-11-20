<template>
  <div id="page" :class="{unload}">
    <router-view></router-view>
  </div>
</template>

<script>
import {mapState} from 'vuex'

export default {
  name: 'App',
  computed: {
    ...mapState([
      'unload'
    ])
  }
}
</script>

<style>
.unload {
  display: none;
}
</style>

<template>
  <div>
    <h1 id="logo"><span id="logo-figure" class="mr-2"></span>{{sitename}}<span class="ml-2 font-weight-normal">{{version}}</span>
    </h1>
    <div id="box" class="text-center">
      {{statusCode}}
    </div>
  </div>
</template>

<script>
import {SITENAME, VERSION} from '@/constants'

export default {
  name: 'ErrorView',
  data () {
    return {
      sitename: SITENAME,
      version: VERSION,
      statusCode: ''
    }
  },
  created () {
    this.statusCode = location.search.remove('?') || 'Error'
    document.title = '{1} - {0}'.format(this.sitename, this.statusCode)
    this.$store.commit('loadComplete')
  },
  mounted () {
    document.body.setAttribute('id', 'error')
  }
}
</script>

<style scoped>
</style>

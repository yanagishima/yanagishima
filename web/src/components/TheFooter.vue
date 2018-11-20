<template>
  <footer id="footer" class="py-3">
    <address class="text-center">
      <span><strong><a href="https://github.com/yanagishima/yanagishima"><i class="fab fa-github mr-1"></i>yanagishima</a></strong>
        by wyukawa and okazou
      </span>
      <a v-if="links.bugsFeedback" :href="links.bugsFeedback" class="mr-2">Bugs and Feedback</a>
    </address>
  </footer>
</template>

<script>
import {LINKS} from '@/constants'

export default {
  name: 'TheFooter',
  data () {
    return {
      links: LINKS
    }
  }
}
</script>

<style scoped>
</style>

<template>
  <span v-html="result"></span>
</template>

<script>
export default {
  name: 'BaseAutoLink',
  props: {
    text: {
      type: String,
      required: true
    }
  },
  computed: {
    result () {
      /* eslint-disable no-useless-escape */
      return this.text
        .replace(/((http|https|ftp)(:\/\/[-_.!~*'()a-zA-Z0-9;\/?:@&=+$,%#]+))/g, '<a href="$1" target="_blank">$1</a>')
    }
  }
}
</script>

<style scoped>
</style>

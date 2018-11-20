<template>
  <span v-html="result"></span>
</template>

<script>
export default {
  name: 'BaseHighlight',
  props: {
    sentence: {
      type: String,
      required: true
    },
    keyword: {
      type: String,
      required: true
    }
  },
  computed: {
    result () {
      const sentence = this.sentence
      const keyword = this.keyword.trim()

      if (!keyword) {
        return sentence
      }

      return keyword.split(' ')
        .unique()
        .map(k => new RegExp(`(${RegExp.escape(k)})`, 'ig'))
        .reduce((s, r) => s.replace(r, '<mark>$1</mark>'), sentence)
    }
  }
}
</script>

<style scoped>
</style>

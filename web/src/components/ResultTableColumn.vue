<template>
  <div v-html="result"></div>
</template>

<script>
function isMAP (arg) {
  return /^{.*=.*}$/.test(arg)
}

export default {
  name: 'ResultTableColumn',
  props: {
    value: {
      required: true,
      validator (val) {
        return val === null || typeof val === 'string'
      }
    },
    pretty: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    result () {
      const value = this.value
      const pretty = this.pretty

      if (value === null) {
        return '<span class="text-muted">(null)</span>'
      }

      if (!pretty) {
        return value.escapeHTML()
      }

      try {
        const o = JSON.parse(value)
        if (Object.isObject(o)) {
          return `<pre class="mb-0">${JSON.stringify(o, undefined, 4).escapeHTML()}</pre>`
        }
      } catch (e) {
        if (isMAP(value)) {
          const values = value.slice(1, -1).split(', ')
          const lines = values.map(v => '    ' + v.trim().replace('=', ' = '))
          return '<pre class="mb-0">{{\n{0}\n}}</pre>'.format(lines.join(',\n').escapeHTML())
        }
      }

      return value.escapeHTML()
    }
  }
}
</script>

<style scoped>
</style>

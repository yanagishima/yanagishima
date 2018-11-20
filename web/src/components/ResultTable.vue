<template>
  <div>
    <table class="table table-auto table-bordered table-hover table-responsive">
      <thead>
      <tr>
        <th class="text-muted">line</th>
        <th v-for="item in result.headers" :key="item">{{item}}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(row, rowIndex) in result.results" :key="rowIndex" :class="{'table-info': line === rowIndex + 1}"
          :id="`L${rowIndex + 1}`">
        <td class="text-right" :class="{'text-muted': readonly}">
          <template v-if="readonly">{{rowIndex + 1}}</template>
          <a v-else href="#" @click.prevent="emitLineClick(rowIndex)" class="text-muted">{{rowIndex + 1}}</a>
        </td>
        <td v-for="(col, colIndex) in row" :key="colIndex">
          <ResultTableColumn :value="col" :pretty="pretty"/>
        </td>
      </tr>
      </tbody>
    </table>
    <div class="text-left" v-if="result.lineNumber > 501">This data is only top 500.</div>
  </div>
</template>

<script>
import ResultTableColumn from '@/components/ResultTableColumn'

export default {
  name: 'ResultTable',
  components: {ResultTableColumn},
  props: {
    result: {
      type: Object,
      required: true
    },
    pretty: {
      type: Boolean,
      default: false
    },
    line: {
      type: Number,
      default: 0
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    emitLineClick (index) {
      this.$emit('line-click', this.line === index + 1 ? 0 : index + 1)
    }
  }
}
</script>

<style scoped>
</style>

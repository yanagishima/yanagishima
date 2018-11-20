import {DATE_COLUMN_NAMES, CHART_TYPES} from '@/constants'

export default {
  computed: {
    validChartTypes () {
      const r = this.response
      return Object.entries(CHART_TYPES)
        .filter(([key, val]) => val.minRows <= r.headers.length)
        .reduce((obj, [key, val]) => Object.assign(obj, {[key]: val}), {})
    },
    chartColumns () {
      const r = this.response

      if (!r || !r.headers || r.headers.length <= 1) {
        return []
      }

      const columns = r.headers.map(h => ({type: 'number', label: h}))
      columns[0].type = DATE_COLUMN_NAMES.includes(columns[0].label) ? 'date' : 'string'

      return columns
    },
    chartRows () {
      const r = this.response
      const columns = this.chartColumns

      if (!r || !r.results || !columns.length) {
        return []
      }

      const rows = []
      for (const res of r.results) {
        const row = []
        if (columns[0].type === 'date') {
          if (!/^[0-9]{8}$/.test(res[0])) {
            return []
          }
          row.push(Date.create(res[0]))
        } else {
          row.push(res[0])
        }

        for (let i = 1; i < res.length; i++) {
          const val = res[i] === 'null' ? 0 : Number(res[i])
          if (Number.isNaN(val)) {
            return []
          }
          row.push(val)
        }

        rows.push(row)
      }

      return rows
    },
    enableChart () {
      return this.chartColumns.length && this.chartRows.length
    }
  }
}

import {DATE_COLUMN_NAMES} from '@/constants'

export default {
  computed: {
    pivotColumns () {
      const r = this.response

      // need more than 2 columns(e.g. dimension1, dimension2, metric)
      if (!r || !r.headers || r.headers.length <= 2) {
        return []
      }

      const columns = r.headers
      // Last column must not be date column
      if (DATE_COLUMN_NAMES.includes(columns[columns.length - 1])) {
        return []
      }
      return columns
    },
    enablePivot () {
      return this.pivotColumns.length && this.pivotRows.length
    },
    rowFields () {
      const columns = this.pivotColumns
      const rows = []
      for (let i = 0; i < columns.length - 2; i++) {
        rows.push({getter: item => item[columns[i]], label: columns[i]})
      }
      return rows
    },
    colFields () {
      const columns = this.pivotColumns
      return [{
        getter: item => item[columns[columns.length - 2]],
        label: columns[columns.length - 2]
      }]
    },
    pivotRows () {
      const r = this.response
      const columns = this.pivotColumns

      if (!r || !r.results || !columns.length) {
        return []
      }

      const rows = []
      for (const res of r.results) {
        const row = {}
        for (let i = 0; i < res.length - 1; i++) {
          row[columns[i]] = res[i]
        }
        const val = Number(res[res.length - 1])
        if (Number.isNaN(val)) {
          return []
        } else {
          row[columns[res.length - 1]] = val
          rows.push(row)
        }
      }
      return rows
    }
  },
  methods: {
    reducer (sum, item) {
      const columns = this.pivotColumns
      // Last column must be one metric and numeric column like pagieviews
      return sum + item[columns[columns.length - 1]]
    }
  }
}

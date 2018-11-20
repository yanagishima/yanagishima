export default {
  formatNumber (val, option) {
    option = option || null
    return Number(val).format(option)
  },
  formatUnit (val) {
    const reg = /^([0-9.]+)([a-z]+)$/i
    if (reg.test(val)) {
      let value = Number(RegExp.$1)
      let unit = RegExp.$2
      switch (unit) {
        case 'ms':
          value = value / 1000
          unit = 's'
          break
        case 'us':
          value = value / 1000000
          unit = 's'
          break
        case 'ns':
          value = value / 1000000000
          unit = 's'
          break
        default:
      }
      return `${value.ceil(1)}${unit}`
    } else {
      if (Object.isNumber(val)) {
        const value = val / 1000
        const unit = 's'
        return `${value.ceil(1)}${unit}`
      } else {
        return val
      }
    }
  },
  extractDate (val) {
    const dt = val.first(19)
    return Date.create(dt).format('{yyyy}/{MM}/{dd} {24hr}:{mm}:{ss}')
  },
  relativeDate (val) {
    if (val) {
      const dt = val.first(19)
      return Date.create(dt).relative()
      // return d.isToday() ? d.format('{24hr}:{mm}') : d.relative();
    }
  },
  humanize (val) {
    if (val) {
      return val.replace(/_/g, ' ').capitalize(true, true)
    }
  }
}

export function calcPrestoQueryProgress (stats, digit) {
  digit = digit || 0
  if (stats.completedDrivers !== undefined && stats.totalDrivers !== undefined) {
    const p = ((stats.completedDrivers / stats.totalDrivers) * 100).ceil(digit)
    return Number.isNaN(p) ? 0 : p
  } else {
    return 0
  }
}

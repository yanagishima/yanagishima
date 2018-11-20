export function getItem (key, def) {
  const val = localStorage.getItem(key)
  if (val === null && def !== undefined) {
    return def
  }
  return val
}

export function getItemNumber (key, def) {
  const val = localStorage.getItem(key)
  if (val === null) {
    if (def !== undefined) {
      return def
    }
    return null
  }
  return Number(val)
}

export function getItemBoolean (key, def) {
  const val = localStorage.getItem(key)
  if (val === null) {
    if (def !== undefined) {
      return def
    }
    return null
  }
  return !!Number(val)
}

export function getItemArray (key, max) {
  const val = localStorage.getItem(key)
  if (!val) {
    return []
  }
  const arr = val.split(',')
  if (max !== undefined) {
    return arr.slice(0, max)
  }
  return arr
}

export function getItemArrayNumber (key, max) {
  return getItemArray(key, max).map(Number)
}

export function getItemJson (key) {
  return JSON.parse(localStorage.getItem(key))
}

export function setItem (key, val) {
  localStorage.setItem(key, val)
}

// save as number (true: 1, false: 0)
export function setItemBoolean (key, val) {
  localStorage.setItem(key, val ? '1' : '0')
}

export function setItemJson (key, val) {
  localStorage.setItem(key, JSON.stringify(val))
}

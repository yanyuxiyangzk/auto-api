export function isValidUrl(string) {
  try {
    new URL(string)
    return true
  } catch (_) {
    return false
  }
}

export function isValidIP(ip) {
  const regex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
  return regex.test(ip)
}

export function isValidPort(port) {
  return Number.isInteger(port) && port > 0 && port <= 65535
}

export function isValidUsername(username) {
  const regex = /^[a-zA-Z][a-zA-Z0-9_]{2,19}$/
  return regex.test(username)
}

export function isValidPassword(password) {
  const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{6,20}$/
  return regex.test(password)
}

export function isValidEmail(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

export function isValidChinese(str) {
  const regex = /[\u4e00-\u9fa5]/
  return regex.test(str)
}

export function isValidTableName(name) {
  const regex = /^[a-z][a-z0-9_]*$/
  return regex.test(name)
}

export function isValidColumnName(name) {
  const regex = /^[a-z][a-z0-9_]*$/
  return regex.test(name)
}

export function isValidIdentifier(name) {
  const regex = /^[a-zA-Z_$][a-zA-Z0-9_$]*$/
  return regex.test(name)
}

export function isValidVersion(version) {
  const regex = /^\d+\.\d+\.\d+$/
  return regex.test(version)
}

export function isPositiveInteger(num) {
  return Number.isInteger(num) && num > 0
}

export function isNonNegativeInteger(num) {
  return Number.isInteger(num) && num >= 0
}

export function isFloat(num, min, max) {
  if (typeof num !== 'number' || isNaN(num)) return false
  if (min !== undefined && num < min) return false
  if (max !== undefined && num > max) return false
  return true
}

export function isValidUsername(username) {
  const reg = /^[a-zA-Z][a-zA-Z0-9_-]{2,16}$/
  return reg.test(username)
}

export function isValidPassword(password) {
  const reg = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{6,20}$/
  return reg.test(password)
}

export function isValidEmail(email) {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

export function isValidUrl(url) {
  const reg = /^(https?:\/\/)?([\w-]+\.)+[\w-]+(\/[\w-./?%&=]*)?$/
  return reg.test(url)
}

export function isValidIP(ip) {
  const reg = /^(\d{1,2}|1\d{2}|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d{2}|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d{2}|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d{2}|2[0-4]\d|25[0-5])$/
  return reg.test(ip)
}

export function isValidPort(port) {
  return port >= 1 && port <= 65535
}

export function isValidTableName(name) {
  const reg = /^[a-z][a-z0-9_]*$/
  return reg.test(name)
}

export function isValidColumnName(name) {
  const reg = /^[a-z][a-z0-9_]*$/
  return reg.test(name)
}

export function isValidSQLType(type) {
  const validTypes = [
    'TINYINT', 'SMALLINT', 'MEDIUMINT', 'INT', 'BIGINT',
    'FLOAT', 'DOUBLE', 'DECIMAL',
    'CHAR', 'VARCHAR', 'TINYTEXT', 'TEXT', 'MEDIUMTEXT', 'LONGTEXT',
    'DATE', 'DATETIME', 'TIMESTAMP', 'TIME', 'YEAR',
    'BINARY', 'VARBINARY', 'BLOB', 'MEDIUMBLOB', 'LONGBLOB',
    'BOOLEAN', 'JSON'
  ]
  return validTypes.some(t => type.toUpperCase().startsWith(t))
}

export function isJsonString(str) {
  try {
    JSON.parse(str)
    return true
  } catch (e) {
    return false
  }
}

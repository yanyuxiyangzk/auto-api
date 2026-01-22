import request from './index'

export function scanTables(datasourceId) {
  return request.get(`/tables/scan/${datasourceId}`)
}

export function getTableList(params) {
  return request.get('/tables', { params })
}

export function getTableDetail(tableName) {
  return request.get(`/tables/${tableName}`)
}

export function getGenerationStatus() {
  return request.get('/tables/status')
}

export function saveTableSelections(data) {
  return request.post('/tables/selections', data)
}

export function generateApi(datasourceId) {
  return request.post('/tables/generate', { datasourceId })
}

export function refreshApi() {
  return request.post('/tables/refresh')
}

export function removeApi(tableName) {
  return request.post(`/tables/${tableName}/remove`)
}

export function forceGenerate(tableName) {
  return request.post(`/tables/${tableName}/force-generate`)
}

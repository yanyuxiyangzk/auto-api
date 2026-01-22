import request from '@/api'

export function scanTables(datasourceId) {
  return request({
    url: `/tables/scan/${datasourceId}`,
    method: 'get'
  })
}

export function getTableSelections(datasourceId) {
  return request({
    url: `/tables/selections/${datasourceId}`,
    method: 'get'
  })
}

export function saveTableSelections(data) {
  return request({
    url: '/tables/selections',
    method: 'post',
    data
  })
}

export function getGenerationStatus() {
  return request({
    url: '/tables/status',
    method: 'get'
  })
}

export function generateApi(datasourceId) {
  return request({
    url: '/tables/generate',
    method: 'post',
    params: { datasourceId }
  })
}

export function refreshApi() {
  return request({
    url: '/tables/refresh',
    method: 'post'
  })
}

export function removeApi(tableName) {
  return request({
    url: `/tables/${tableName}/remove`,
    method: 'post'
  })
}

export function forceGenerate(tableName) {
  return request({
    url: `/tables/${tableName}/force-generate`,
    method: 'post'
  })
}

export function getTableData(tableName, params) {
  return request({
    url: `/tables/${tableName}/data`,
    method: 'get',
    params
  })
}

export function createTableData(tableName, data) {
  return request({
    url: `/tables/${tableName}/data`,
    method: 'post',
    data
  })
}

export function updateTableData(tableName, id, data) {
  return request({
    url: `/tables/${tableName}/data/${id}`,
    method: 'put',
    data
  })
}

export function deleteTableData(tableName, id) {
  return request({
    url: `/tables/${tableName}/data/${id}`,
    method: 'delete'
  })
}

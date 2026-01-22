import request from './index'

export function getDatasourceList(params) {
  return request.get('/datasources', { params })
}

export function getDatasourceDetail(id) {
  return request.get(`/datasources/${id}`)
}

export function createDatasource(data) {
  return request.post('/datasources', data)
}

export function updateDatasource(id, data) {
  return request.put(`/datasources/${id}`, data)
}

export function deleteDatasource(id) {
  return request.delete(`/datasources/${id}`)
}

export function testDatasource(id) {
  return request.post(`/datasources/${id}/test`)
}

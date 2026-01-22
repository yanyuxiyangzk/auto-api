import request from './index'

export function getScriptList(params) {
  return request.get('/scripts', { params })
}

export function getScriptDetail(id) {
  return request.get(`/scripts/${id}`)
}

export function createScript(data) {
  return request.post('/scripts', data)
}

export function updateScript(id, data) {
  return request.put(`/scripts/${id}`, data)
}

export function deleteScript(id) {
  return request.delete(`/scripts/${id}`)
}

export function executeScript(id, params) {
  return request.post(`/scripts/${id}/execute`, params)
}

export function getScriptVersions(id) {
  return request.get(`/scripts/${id}/versions`)
}

export function rollbackScript(id, version) {
  return request.post(`/scripts/${id}/rollback`, { version })
}

import request from './index'

export function previewDDL(data) {
  return request.post('/table-create/preview', data)
}

export function executeDDL(data) {
  return request.post('/table-create/execute', data)
}

export function getTableTemplates() {
  return request.get('/table-create/templates')
}

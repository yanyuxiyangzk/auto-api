import request from '@/api'

export function previewDDL(data) {
  return request({
    url: '/table-create/preview',
    method: 'post',
    data
  })
}

export function executeDDL(data) {
  return request({
    url: '/table-create/execute',
    method: 'post',
    data
  })
}

export function getTableTemplates() {
  return request({
    url: '/table-create/templates',
    method: 'get'
  })
}

export function getTableCreateHistory(params) {
  return request({
    url: '/table-create/history',
    method: 'get',
    params
  })
}

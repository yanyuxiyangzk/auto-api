import request from '@/api'

export function getDatasourceList() {
  return request({
    url: '/datasources',
    method: 'get'
  })
}

export function getDatasource(id) {
  return request({
    url: `/datasources/${id}`,
    method: 'get'
  })
}

export function createDatasource(data) {
  return request({
    url: '/datasources',
    method: 'post',
    data
  })
}

export function updateDatasource(id, data) {
  return request({
    url: `/datasources/${id}`,
    method: 'put',
    data
  })
}

export function deleteDatasource(id) {
  return request({
    url: `/datasources/${id}`,
    method: 'delete'
  })
}

export function testDatasource(id) {
  return request({
    url: `/datasources/${id}/test`,
    method: 'post'
  })
}

export function testDatasourceConnection(data) {
  return request({
    url: '/datasources/test',
    method: 'post',
    data
  })
}

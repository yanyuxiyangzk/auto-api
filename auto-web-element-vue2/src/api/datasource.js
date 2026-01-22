import request from '@/api'

export function getDatasourceList() {
  return request({
    url: '/datasource/list',
    method: 'get'
  })
}

export function getDatasource(id) {
  return request({
    url: `/datasource/${id}`,
    method: 'get'
  })
}

export function createDatasource(data) {
  return request({
    url: '/datasource/create',
    method: 'post',
    data
  })
}

export function updateDatasource(id, data) {
  return request({
    url: `/datasource/update/${id}`,
    method: 'put',
    data
  })
}

export function deleteDatasource(id) {
  return request({
    url: `/datasource/delete/${id}`,
    method: 'delete'
  })
}

export function testDatasourceConnection(data) {
  return request({
    url: '/datasource/test',
    method: 'post',
    data
  })
}

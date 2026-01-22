import request from '@/api'

export function getScriptList(params) {
  return request({
    url: '/scripts',
    method: 'get',
    params
  })
}

export function getScript(id) {
  return request({
    url: `/scripts/${id}`,
    method: 'get'
  })
}

export function createScript(data) {
  return request({
    url: '/scripts',
    method: 'post',
    data
  })
}

export function updateScript(id, data) {
  return request({
    url: `/scripts/${id}`,
    method: 'put',
    data
  })
}

export function deleteScript(id) {
  return request({
    url: `/scripts/${id}`,
    method: 'delete'
  })
}

export function executeScript(id, data) {
  return request({
    url: `/scripts/${id}/execute`,
    method: 'post',
    data
  })
}

export function getScriptVersions(id) {
  return request({
    url: `/scripts/${id}/versions`,
    method: 'get'
  })
}

export function rollbackScript(id, version) {
  return request({
    url: `/scripts/${id}/rollback`,
    method: 'post',
    params: { version }
  })
}

export function saveScriptVersion(id, data) {
  return request({
    url: `/scripts/${id}/versions`,
    method: 'post',
    data
  })
}

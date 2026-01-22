import request from '@/api'

export function getApiDocs() {
  return request({
    url: '/api-docs',
    method: 'get'
  })
}

export function exportOpenApi() {
  return request({
    url: '/api-docs/export/openapi',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportSwagger() {
  return request({
    url: '/api-docs/export/swagger',
    method: 'get',
    responseType: 'blob'
  })
}

export function getGraphQLSchema() {
  return request({
    url: '/graphql/schema',
    method: 'get'
  })
}

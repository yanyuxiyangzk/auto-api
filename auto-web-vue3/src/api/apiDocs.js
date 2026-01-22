import request from './index'

export function getApiDocs() {
  return request.get('/api-docs')
}

export function getRestApiList() {
  return request.get('/api-docs/rest')
}

export function getGraphQLSchema() {
  return request.get('/api-docs/graphql/schema')
}

export function exportOpenAPI() {
  return request.get('/api-docs/export/openapi', { responseType: 'blob' })
}

export function exportSwagger() {
  return request.get('/api-docs/export/swagger', { responseType: 'blob' })
}

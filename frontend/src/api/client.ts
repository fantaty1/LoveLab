/** Same-origin API transport. The page UI can adopt this incrementally while guests keep local progress. */
export class ApiError extends Error {
  constructor(public status: number, public code: string, message: string, public requestId?: string) { super(message) }
}

let csrf: { token: string; headerName: string } | undefined
async function csrfToken() {
  if (!csrf) csrf = await api<{ token: string; headerName: string }>('/auth/csrf')
  return csrf
}

export async function api<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const token = await csrfToken(); headers[token.headerName] = token.token
  }
  const response = await fetch(`/api/v1${path}`, { method, headers, credentials: 'include', ...(body === undefined ? {} : { body: JSON.stringify(body) }) })
  if (!response.ok) {
    const error = await response.json().catch(() => ({ code: 'HTTP_ERROR', message: '服务暂时不可用' }))
    if (response.status === 401 || response.status === 403) csrf = undefined
    throw new ApiError(response.status, error.code, error.message, error.requestId)
  }
  if (path === '/auth/login' || path === '/auth/logout') csrf = undefined
  return response.status === 204 ? undefined as T : await response.json() as T
}

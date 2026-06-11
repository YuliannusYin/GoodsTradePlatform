import axios from 'axios'
import type { Method } from 'axios'
import { useAuthenticationStore } from '../authenticationStore'
import { useLoadingStore } from './loadingStore'

const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

export class ApiError extends Error {
  public status: number
  public data: any
  public url: string
  public method: string

  constructor(status: number, data: any, url: string = '', method: string = '') {
    super(data?.message || '请求失败')
    this.status = status
    this.data = data
    this.url = url
    this.method = method
  }
}

export async function callGet(endpoint: string) {
  return await makeRequest('GET', endpoint)
}

export async function callPost(endpoint: string, data: any) {
  return await makeRequest('POST', endpoint, data)
}

export async function callPut(endpoint: string, data: any) {
  return await makeRequest('PUT', endpoint, data)
}

export async function callPatch(endpoint: string, data: any) {
  return await makeRequest('PATCH', endpoint, data)
}

export async function callDelete(endpoint: string) {
  return await makeRequest('DELETE', endpoint, undefined)
}

async function makeRequest(method: Method, endpoint: string, data?: any) {
  setIsLoading(true)

  try {
    const url = `${baseUrl}${endpoint}`
    const jwtToken = useAuthenticationStore().methods.getJwtToken()

    const result = await axios.request({
      method,
      url,
      data,
      headers: { Authorization: jwtToken ? jwtToken : null }
    })

    setIsLoading(false)
    return result.data
  } catch (error: any) {
    setIsLoading(false)
    const requestUrl = `${baseUrl}${endpoint}`
    if (error.response) {
      throw new ApiError(error.response.status, error.response.data, requestUrl, method)
    }
    throw new ApiError(0, { error: true, message: error.message || '网络错误' }, requestUrl, method)
  }
}

function setIsLoading(state: boolean) {
  useLoadingStore().methods.setIsLoading(state)
}

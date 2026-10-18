import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 如果是 blob 类型响应（文件下载），返回 data 的同时保留响应头
    // （页面需要读取 Content-Disposition 以使用服务端文件名）。
    // 同时保持 Blob 的既有形态：仍可直接当 Blob 使用（如 new Blob([res])），
    // 仅在需要响应头时读取 data / headers。
    if (response.config.responseType === 'blob') {
      return withResponseMeta(response.data as Blob, response.headers ?? {})
    }
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    const err = new Error(message || '请求失败') as Error & { code: number; data: unknown }
    err.code = code
    err.data = data
    if (code !== 409) {
      ElMessage.error(message || '请求失败')
    }
    return Promise.reject(err)
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/#/login'
        ElMessage.warning(data.message || '登录已过期,请重新登录')
      } else if (status === 403) {
        ElMessage.error('权限不足,无法访问')
      } else if (status === 409) {
        return Promise.reject(error.response.data)
      } else {
        ElMessage.error(data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络异常,请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/**
 * 带有响应头的 Blob
 *
 * 既可用作普通 Blob（`new Blob([res])`、`URL.createObjectURL(res)` 均有效），
 * 也可通过 `data` / `headers` 读取原始文件与响应头。
 * 这样新增的下载能力不会破坏既有的“直接当 Blob 用”的调用点。
 */
export interface BlobWithHeaders extends Blob {
  data: Blob
  headers: Record<string, string | undefined>
}

function withResponseMeta(blob: Blob, headers: Record<string, any>): BlobWithHeaders {
  Object.defineProperties(blob, {
    data: { value: blob, enumerable: true, configurable: true },
    headers: { value: headers, enumerable: true, configurable: true }
  })
  return blob as BlobWithHeaders
}

export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service(config) as Promise<T>
}

export function get<T>(url: string, params?: object): Promise<T> {
  return request({ method: 'GET', url, params })
}

export function post<T>(url: string, data?: object): Promise<T> {
  return request({ method: 'POST', url, data })
}

export function put<T>(url: string, data?: object): Promise<T> {
  return request({ method: 'PUT', url, data })
}

export function del<T>(url: string, params?: object): Promise<T> {
  return request({ method: 'DELETE', url, params })
}

export default service
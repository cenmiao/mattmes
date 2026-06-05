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
    // 如果是 blob 类型响应（文件下载），直接返回 data
    if (response.config.responseType === 'blob') {
      return response.data
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
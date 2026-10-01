import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 60_000,
  headers: { 'Content-Type': 'application/json' },
})

// Response interceptor — surface API errors globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      'Lỗi kết nối máy chủ. Vui lòng thử lại.'

    // Don't toast for polling 404s (contract still processing)
    const isPolling = error.config?.url?.includes('/status')
    if (!isPolling) {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default api

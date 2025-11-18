import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api'

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Auth API
export const authAPI = {
  login: (email: string, password: string) =>
    apiClient.post('/auth/login', { email, password }),
  
  register: (data: {
    email: string
    password: string
    firstName: string
    lastName: string
    phone?: string
  }) => apiClient.post('/auth/register', data),
  
  refreshToken: () => apiClient.post('/auth/refresh'),
}

// Appointment API
export const appointmentAPI = {
  create: (data: {
    staffId: number
    serviceId: number
    startTime: string
    endTime: string
    notes?: string
  }) => apiClient.post('/appointments', data),
  
  getAll: (params?: { userId?: number; staffId?: number; from?: string; to?: string }) =>
    apiClient.get('/appointments', { params }),
  
  getById: (id: number) => apiClient.get(`/appointments/${id}`),
  
  update: (id: number, data: any) => apiClient.patch(`/appointments/${id}`, data),
  
  cancel: (id: number, reason: string) =>
    apiClient.delete(`/appointments/${id}`, { data: { reason } }),
}

// Staff API
export const staffAPI = {
  getAll: () => apiClient.get('/staff'),
  
  getById: (id: number) => apiClient.get(`/staff/${id}`),
  
  getAvailability: (id: number, date: string) =>
    apiClient.get(`/staff/${id}/availability`, { params: { date } }),
  
  updateSchedule: (id: number, data: any) =>
    apiClient.patch(`/staff/${id}/schedule`, data),
}

// Service API
export const serviceAPI = {
  getAll: () => apiClient.get('/services'),
  
  getById: (id: number) => apiClient.get(`/services/${id}`),
}

// Admin API
export const adminAPI = {
  getAnalytics: () => apiClient.get('/admin/analytics'),
  
  getActivityLog: () => apiClient.get('/admin/activity-log'),
  
  getStaffLoad: () => apiClient.get('/admin/staff-load'),
}

// User API
export const userAPI = {
  getProfile: () => apiClient.get('/users/me'),
  
  updateProfile: (data: any) => apiClient.patch('/users/me', data),
}

export default apiClient

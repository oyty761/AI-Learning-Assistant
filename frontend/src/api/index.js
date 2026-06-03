import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// 笔记模块API
export const notesApi = {
  uploadFile: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/notes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  generateNotes: (data) => api.post('/notes/generate', data),
  getNotes: (userId) => api.get('/notes/list', { params: { userId } }),
  getNoteById: (id) => api.get(`/notes/${id}`),
  deleteNote: (id) => api.delete(`/notes/${id}`)
}

// 对话导师模块API
export const tutorApi = {
  ask: (userId, question, sessionId) => api.post('/tutor/ask', { userId, question, sessionId }),
  getHistory: (userId) => api.get('/tutor/history', { params: { userId } }),
  getSessions: (userId) => api.get('/tutor/sessions', { params: { userId } }),
  getSessionMessages: (sessionId) => api.get(`/tutor/session/${sessionId}/messages`),
  deleteSession: (sessionId) => api.delete(`/tutor/session/${sessionId}`)
}

// 解题诊断模块API
export const diagnoseApi = {
  uploadAndOcr: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/diagnose/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  analyze: (data) => api.post('/diagnose/analyze', null, { params: data }),
  getRecords: (userId) => api.get('/diagnose/records', { params: { userId } }),
  getProfile: (userId) => api.get('/diagnose/profile', { params: { userId } })
}

// 出题教练模块API
export const examApi = {
  generate: (userId) => api.post('/exam/generate', { userId }),
  generateSpecific: (knowledgePoint, errorType) => 
    api.post('/exam/generate-specific', { knowledgePoint, errorType }),
  getProfile: (userId) => api.get(`/exam/profile/${userId}`),
  shouldRecommend: (userId) => api.get('/exam/should-recommend', { params: { userId } })
}

// 待办事项模块API
export const todoApi = {
  create: (data) => api.post('/todos', data),
  getList: (userId, completed) => api.get('/todos', { params: { userId, completed } }),
  getStats: (userId) => api.get('/todos/stats', { params: { userId } }),
  update: (id, data) => api.put(`/todos/${id}`, data),
  toggle: (id, userId) => api.put(`/todos/${id}/toggle`, { userId }),
  delete: (id, userId) => api.delete(`/todos/${id}`, { params: { userId } })
}

// 学习分析模块API
export const analysisApi = {
  getDashboard: (userId) => api.get('/analysis/dashboard', { params: { userId } }),
  getActivityCalendar: (userId) => api.get('/analysis/activity-calendar', { params: { userId } }),
  getReport: (userId) => api.get('/analysis/report', { params: { userId } })
}

export default api

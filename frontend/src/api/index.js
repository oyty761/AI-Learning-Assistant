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
  generateNotes: (data) => api.post('/notes/generate', null, { params: data }),
  getNotes: (userId) => api.get('/notes/list', { params: { userId } }),
  getNoteById: (id) => api.get(`/notes/${id}`),
  deleteNote: (id) => api.delete(`/notes/${id}`)
}

// 对话导师模块API
export const tutorApi = {
  ask: (userId, question) => api.post('/tutor/ask', { userId, question }),
  getHistory: (userId) => api.get('/tutor/history', { params: { userId } })
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

export default api

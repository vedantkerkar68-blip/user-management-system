import api from './api';

export const employeeApi = {
  getAll: (params = {}) => {
    const queryParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        queryParams.append(key, value);
      }
    });
    return api.get(`/api/employees?${queryParams.toString()}`);
  },

  getById: (id) => api.get(`/api/employees/${id}`),

  create: (employee) => api.post('/api/employees', employee),

  update: (id, employee) => api.put(`/api/employees/${id}`, employee),

  updateStatus: (id, status) => api.patch(`/api/employees/${id}/status`, null, { params: { status } }),

  delete: (id) => api.delete(`/api/employees/${id}`),

  getDashboardStats: () => api.get('/api/employees/dashboard/stats'),

  getDepartments: () => api.get('/api/employees/departments'),
};
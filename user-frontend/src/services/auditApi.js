import api from './api';

export const auditApi = {
  getAll: (params = {}) => {
    const queryParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        queryParams.append(key, value);
      }
    });
    return api.get(`/api/audit?${queryParams.toString()}`);
  },

  getStats: () => api.get('/api/audit/stats'),

  getActions: () => api.get('/api/audit/actions'),
};
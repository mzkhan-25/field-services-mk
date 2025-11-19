import apiClient from './apiClient';

const authAPI = {
  login: async (username, password) => {
    const response = await apiClient.post('/auth/login', {
      username,
      password,
    });
    return response.data;
  },

  healthCheck: async () => {
    const response = await apiClient.get('/auth/health');
    return response.data;
  },
};

export default authAPI;

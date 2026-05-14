import axios from 'axios';

// Create an Axios instance
const api = axios.create({
  // Point this to your API Gateway or directly to services if not using Gateway
  baseURL: 'http://localhost:8085', // Assuming your API Gateway is on 8085
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token if needed
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;

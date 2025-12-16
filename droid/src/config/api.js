/**
 * API Configuration for Mobile App
 * 
 * Change API_BASE_URL to point to your backend server:
 * - Development (emulator): http://10.0.2.2:8080
 * - External server: http://YOUR_SERVER_IP:8080
 * - Production: https://your-domain.com
 */

export const API_BASE_URL = 'http://135.148.149.138:8080';

export const API_ENDPOINTS = {
  LOGIN: '/api/emails/login',
  REGISTER: '/api/emails/register',
  EMAILS: '/api/emails',
  CHAT: '/api/chat/message',
  CHAT_HISTORY: '/api/chat/history',
};

// Global token manager - maintained in app runtime context
let currentToken = null;
let tokenUpdateCallback = null;

export const setToken = (token) => {
  currentToken = token;
};

export const getToken = () => {
  return currentToken;
};

export const clearToken = () => {
  currentToken = null;
};

export const setTokenUpdateCallback = (callback) => {
  tokenUpdateCallback = callback;
};

/**
 * Helper function to make authenticated API calls
 * Automatically includes JWT token in Authorization header
 * Automatically extracts and updates token from X-New-JWT response header
 */
export const apiFetch = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Include JWT token in Authorization header if available
  if (currentToken) {
    headers['Authorization'] = `Bearer ${currentToken}`;
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  // Check for refreshed token in response headers (sliding renewal)
  const newToken = response.headers.get('X-New-JWT');
  if (newToken) {
    currentToken = newToken;
    if (tokenUpdateCallback) {
      tokenUpdateCallback(newToken);
    }
  }

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  return response.json();
};

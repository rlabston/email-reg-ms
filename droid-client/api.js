// Basic API client for gateway communication
export const API_BASE_URL = 'http://localhost:8080'; // Change to your gateway URL

export async function fetchServices() {
  const response = await fetch(`${API_BASE_URL}/api/services`);
  if (!response.ok) throw new Error('Failed to fetch services');
  return response.json();
}

export async function login(email, password) {
  const response = await fetch(`${API_BASE_URL}/api/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  if (!response.ok) throw new Error('Login failed');
  return response.json();
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  email: string;
  username: string;
  message: string;
  roles?: string[];
  token?: string;
  expiresInMs?: number;
}

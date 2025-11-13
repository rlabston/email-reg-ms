export interface EmailRegistrationRequest {
  email: string;
  username: string;
  password: string;
}

export interface EmailRegistrationResponse {
  id: number;
  email: string;
  username: string;
  registrationDate: string;
  message: string;
}

import { api } from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types';

export const authApi = {
    register: (data: RegisterRequest) =>
        api.post<AuthResponse>('/api/v1/auth/register', data).then((r) => r.data),

    login: (data: LoginRequest) =>
        api.post<AuthResponse>('/api/v1/auth/login', data).then((r) => r.data),

    me: () =>
        api.get('/api/v1/auth/me').then((r) => r.data),
};
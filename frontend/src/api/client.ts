import axios from 'axios';

const TOKEN_KEY = 'mockit_token';

// База API: на проді (S3) береться з VITE_API_URL (адреса бекенду на AWS),
// локально порожня - працює проксі Vite (/api -> localhost:8080)
const API_BASE = import.meta.env.VITE_API_URL || '';

// базовий клієнт
export const api = axios.create({
    baseURL: API_BASE,
    headers: { 'Content-Type': 'application/json' },
});

// --- робота з токеном ---
export const tokenStorage = {
    get: () => localStorage.getItem(TOKEN_KEY),
    set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
    clear: () => localStorage.removeItem(TOKEN_KEY),
};

// REQUEST інтерсептор: підкладає Bearer-токен у кожен запит
api.interceptors.request.use((config) => {
    const token = tokenStorage.get();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// RESPONSE інтерсептор: якщо 401 - токен протух, чистимо і кидаємо на логін
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            tokenStorage.clear();
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

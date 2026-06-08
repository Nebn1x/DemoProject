import axios from 'axios';

const TOKEN_KEY = 'mockit_token';

// базовий клієнт (baseURL не треба - проксі Vite веде /api на бекенд)
export const api = axios.create({
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
            // редірект на логін, якщо ще не там
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);
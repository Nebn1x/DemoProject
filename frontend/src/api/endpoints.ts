import { api } from './client';
import type { MockEndpoint, CreateEndpointRequest, Page, RequestLog } from '../types';

export const endpointsApi = {
    // Отримати всі ендпоінти
    list: (page = 0, size = 20) =>
        api.get<Page<MockEndpoint>>(`/api/v1/endpoints`, {
            params: { page, size },
        }),

    // Отримати один ендпоінт
    getById: (id: string) =>
        api.get<MockEndpoint>(`/api/v1/endpoints/${id}`),

    // Отримати останні запити (логи) для ендпоінта, пагіновано
    getLogs: (id: string, page = 0, size = 20) =>
        api.get<Page<RequestLog>>(`/api/v1/endpoints/${id}/logs`, {
            params: { page, size, sort: 'timestamp,desc' },
        }),

    // Створити новий
    create: (data: CreateEndpointRequest) =>
        api.post<MockEndpoint>('/api/v1/endpoints', data),

    // Оновити ендпоінт
    update: (id: string, data: Partial<CreateEndpointRequest>) =>
        api.put<MockEndpoint>(`/api/v1/endpoints/${id}`, data),

    // Видалити ендпоінт
    delete: (id: string) =>
        api.delete<void>(`/api/v1/endpoints/${id}`),

    // Тест: викликати mock-URL і повернути відповідь (статус + тіло)
    test: async (endpoint: MockEndpoint) => {
        const start = performance.now();
        const res = await fetch(endpoint.fullUrl, { method: endpoint.method });
        const latency = Math.round(performance.now() - start);
        const body = await res.text();
        return { status: res.status, body, latency };
    },
};
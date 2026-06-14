import { api } from './client';
import type { MockEndpoint, CreateEndpointRequest, Page } from '../types';

export const endpointsApi = {
    // Отримати всі ендпоінти
    list: (page = 0, size = 20) =>
        api.get<Page<MockEndpoint>>(`/api/v1/endpoints`, {
            params: { page, size },
        }),

    // Отримати один ендпоінт
    getById: (id: string) =>
        api.get<MockEndpoint>(`/api/v1/endpoints/${id}`),

    // Створити новий
    create: (data: CreateEndpointRequest) =>
        api.post<MockEndpoint>('/api/v1/endpoints', data),

    // Оновити ендпоінт
    update: (id: string, data: Partial<CreateEndpointRequest>) =>
        api.put<MockEndpoint>(`/api/v1/endpoints/${id}`, data),

    // Видалити ендпоінт
    delete: (id: string) =>
        api.delete<void>(`/api/v1/endpoints/${id}`),
};
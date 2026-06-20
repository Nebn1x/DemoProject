// Відповідає бекенд-DTO

export interface AuthResponse {
    token: string;
    tokenType: string;
    expiresInMs: number;
    userId: string;
    email: string;
    userHash: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface MockEndpoint {
    id: string;
    method: string;
    path: string;
    fullUrl: string;
    responseBody: string | null;
    responseStatus: number;
    contentType: string;
    delayMs: number;
    expiresAt: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CreateEndpointRequest {
    method: string;
    path: string;
    responseBody?: string;
    responseStatus?: number;
    contentType?: string;
    delayMs?: number;
    ttlHours?: number;
}

// Spring Page<T>
export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export interface ApiError {
    error: string;
    message: string;
    timestamp: string;
    status: number;
    details?: Record<string, string>;
}

export interface UpdateEndpointRequest extends Partial<CreateEndpointRequest> {}

export interface RequestLog {
    id: string;
    method: string;
    path: string;
    status: number;
    latencyMs: number;
    timestamp: string;
}
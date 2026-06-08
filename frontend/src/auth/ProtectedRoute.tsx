import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import type { ReactNode } from 'react';

/**
 * Обгортка для сторінок, які потребують авторизації.
 * Якщо не залогінений - кидає на /login.
 */
export default function ProtectedRoute({ children }: { children: ReactNode }) {
    const { isAuthenticated, loading } = useAuth();

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-slate-400">Завантаження...</div>
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
}

import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { tokenStorage } from '../api/client';
import { authApi } from '../api/auth';
import type { AuthResponse } from '../types';

interface AuthContextType {
    isAuthenticated: boolean;
    user: { email: string; userHash: string; userId: string } | null;
    loading: boolean;
    login: (auth: AuthResponse) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthContextType['user']>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = tokenStorage.get();
        if (token) {
            authApi.me()
                .then((data) => {
                    setUser({ email: data.email, userHash: data.userHash, userId: data.userId });
                })
                .catch(() => {
                    tokenStorage.clear();
                })
                .finally(() => setLoading(false));
        } else {
            setLoading(false);
        }
    }, []);

    const login = (auth: AuthResponse) => {
        tokenStorage.set(auth.token);
        setUser({ email: auth.email, userHash: auth.userHash, userId: auth.userId });
    };

    const logout = () => {
        tokenStorage.clear();
        setUser(null);
    };

    return (
        <AuthContext.Provider
            value={{ isAuthenticated: !!user, user, loading, login, logout }}
        >
            {children}
        </AuthContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth має використовуватись всередині AuthProvider');
    return ctx;
}

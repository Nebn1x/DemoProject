import React, { useState, useCallback } from 'react';

export type ToastType = 'success' | 'error' | 'info';

interface ToastItem {
    id: string;
    message: string;
    type: ToastType;
}

interface ToastContextType {
    addToast: (message: string, type?: ToastType) => void;
}

const ToastContext = React.createContext<ToastContextType | undefined>(undefined);

const STYLES: Record<ToastType, { bg: string; icon: string }> = {
    success: { bg: 'bg-green-500', icon: '✓' },
    error: { bg: 'bg-red-500', icon: '✕' },
    info: { bg: 'bg-blue-500', icon: 'ℹ' },
};

export function ToastProvider({ children }: { children: React.ReactNode }) {
    const [toasts, setToasts] = useState<ToastItem[]>([]);

    const removeToast = useCallback((id: string) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    }, []);

    const addToast = useCallback((message: string, type: ToastType = 'info') => {
        const id = Date.now().toString() + Math.random().toString(36).slice(2);
        setToasts((prev) => [...prev, { id, message, type }]);
        setTimeout(() => removeToast(id), 3000);
    }, [removeToast]);

    return (
        <ToastContext.Provider value={{ addToast }}>
            {children}
            <div className="fixed bottom-4 right-4 z-50 space-y-2">
                {toasts.map((toast) => {
                    const style = STYLES[toast.type];
                    return (
                        <div
                            key={toast.id}
                            className={`${style.bg} text-white px-4 py-3 rounded-lg shadow-lg flex items-center gap-3 animate-in fade-in slide-in-from-bottom-4 duration-300`}
                        >
                            <span className="font-bold text-lg">{style.icon}</span>
                            <p className="text-sm">{toast.message}</p>
                        </div>
                    );
                })}
            </div>
        </ToastContext.Provider>
    );
}

export function useToast() {
    const context = React.useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within ToastProvider');
    }
    return context;
}

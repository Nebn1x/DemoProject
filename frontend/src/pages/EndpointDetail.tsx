import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { endpointsApi } from '../api/endpoints';
import type { MockEndpoint, CreateEndpointRequest } from '../types';
import { useToast } from '../components/Toast';
import EndpointSettingsForm from '../components/EndpointSettingsForm';
import EndpointLogs from '../components/EndpointLogs';

export default function EndpointDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth();
    const { addToast } = useToast();

    const [endpoint, setEndpoint] = useState<MockEndpoint | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [notFound, setNotFound] = useState(false);
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        if (!id) return;
        let cancelled = false;

        setIsLoading(true);
        endpointsApi
            .getById(id)
            .then((res) => {
                if (!cancelled) setEndpoint(res.data);
            })
            .catch((err) => {
                if (cancelled) return;
                if (err.response?.status === 404) {
                    setNotFound(true);
                } else {
                    addToast('Помилка завантаження ендпоінта', 'error');
                }
                console.error(err);
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const handleSave = async (data: Partial<CreateEndpointRequest>) => {
        if (!id) return;
        try {
            const res = await endpointsApi.update(id, data);
            setEndpoint(res.data);
            addToast('Ендпоінт оновлено', 'success');
        } catch (err) {
            addToast('Помилка при оновленні', 'error');
            console.error(err);
        }
    };

    const handleCopyUrl = () => {
        if (!endpoint) return;
        navigator.clipboard.writeText(endpoint.fullUrl);
        setCopied(true);
        addToast('URL скопійовано', 'success');
        setTimeout(() => setCopied(false), 1500);
    };

    if (notFound) {
        return (
            <div className="min-h-screen bg-slate-50 flex items-center justify-center">
                <div className="text-center">
                    <p className="text-slate-500 mb-4">Ендпоінт не знайдено.</p>
                    <button
                        onClick={() => navigate('/dashboard')}
                        className="px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded hover:bg-blue-700 transition"
                    >
                        ← До списку ендпоінтів
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-slate-50">
            {/* Шапка */}
            <header className="bg-white border-b border-slate-200">
                <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
                    <h1
                        className="text-2xl font-bold text-slate-800 cursor-pointer"
                        onClick={() => navigate('/dashboard')}
                    >
                        🚀 Mock-It
                    </h1>
                    <span className="text-sm text-slate-500">{user?.email}</span>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-4 py-8">
                <button
                    onClick={() => navigate('/dashboard')}
                    className="text-sm text-slate-500 hover:text-slate-800 transition mb-4"
                >
                    ← До списку ендпоінтів
                </button>

                {isLoading || !endpoint ? (
                    <div className="space-y-4">
                        <div className="h-8 w-64 bg-slate-200 rounded animate-pulse" />
                        <div className="h-40 bg-slate-200 rounded animate-pulse" />
                        <div className="h-64 bg-slate-200 rounded animate-pulse" />
                    </div>
                ) : (
                    <>
                        {/* Заголовок ендпоінта + mock URL */}
                        <div className="mb-6 flex items-start justify-between flex-wrap gap-3">
                            <div>
                                <h2 className="text-xl font-bold text-slate-800 mb-1 font-mono">
                                    {endpoint.method} {endpoint.path}
                                </h2>
                                <div className="flex items-center gap-2">
                                    <code className="text-sm text-slate-500 bg-slate-100 px-2 py-1 rounded">
                                        {endpoint.fullUrl}
                                    </code>
                                    <button
                                        onClick={handleCopyUrl}
                                        className="px-2 py-1 text-xs bg-slate-200 text-slate-700 rounded hover:bg-slate-300 transition"
                                    >
                                        {copied ? '✓ Скопійовано' : '📋 Copy'}
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            <EndpointSettingsForm endpoint={endpoint} onSave={handleSave} />
                            <EndpointLogs endpointId={endpoint.id} />
                        </div>
                    </>
                )}
            </main>
        </div>
    );
}
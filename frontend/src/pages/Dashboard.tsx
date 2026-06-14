import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { endpointsApi } from '../api/endpoints';
import type { MockEndpoint, CreateEndpointRequest } from '../types';
import { Modal } from '../components/Modal';
import { useToast } from '../components/Toast';
import CreateEndpointModal from '../components/EndpointModal';

export default function Dashboard() {
    const { user, logout } = useAuth();
    const { addToast } = useToast();

    const [endpoints, setEndpoints] = useState<MockEndpoint[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedId, setSelectedId] = useState<string | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [editForm, setEditForm] = useState<Partial<CreateEndpointRequest>>({});

    // Завантажити список ендпоінтів
    const loadEndpoints = async () => {
        try {
            setIsLoading(true);
            const response = await endpointsApi.list();
            setEndpoints(response.data.content);
        } catch (error) {
            addToast('Помилка завантаження ендпоінтів', 'error');
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadEndpoints();
    }, []);

    // Створити новий ендпоінт
    const handleCreate = async (data: CreateEndpointRequest) => {
        try {
            const response = await endpointsApi.create(data);
            setEndpoints((prev) => [response.data, ...prev]);
            addToast('Ендпоінт створено', 'success');
            setIsCreateModalOpen(false);
        } catch (error) {
            addToast('Помилка при створенні', 'error');
            console.error(error);
        }
    };

    // Видалити ендпоінт
    const handleDelete = async () => {
        if (!selectedId) return;
        try {
            await endpointsApi.delete(selectedId);
            setEndpoints((prev) => prev.filter((e) => e.id !== selectedId));
            addToast('Ендпоінт видалено', 'success');
            setIsModalOpen(false);
            setSelectedId(null);
        } catch (error) {
            addToast('Помилка при видаленні', 'error');
            console.error(error);
        }
    };

    // Копіювати URL
    const handleCopyUrl = (url: string) => {
        navigator.clipboard.writeText(url);
        addToast('URL скопійовано', 'success');
    };

    // Відкрити форму редагування
    const handleEditOpen = (endpoint: MockEndpoint) => {
        setEditingId(endpoint.id);
        setEditForm({
            method: endpoint.method,
            path: endpoint.path,
            responseStatus: endpoint.responseStatus,
            responseBody: endpoint.responseBody || '',
            contentType: endpoint.contentType,
            delayMs: endpoint.delayMs,
        });
    };

    // Зберегти зміни
    const handleSave = async () => {
        if (!editingId) return;
        try {
            const response = await endpointsApi.update(editingId, editForm);
            setEndpoints((prev) =>
                prev.map((e) => (e.id === editingId ? response.data : e))
            );
            addToast('Ендпоінт оновлено', 'success');
            setEditingId(null);
        } catch (error) {
            addToast('Помилка при оновленні', 'error');
            console.error(error);
        }
    };

    if (isLoading) {
        return (
            <div className="min-h-screen bg-slate-50 flex items-center justify-center">
                <p className="text-slate-500">Завантаження...</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-slate-50">
            {/* Шапка */}
            <header className="bg-white border-b border-slate-200">
                <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
                    <h1 className="text-2xl font-bold text-slate-800">🚀 Mock-It</h1>
                    <div className="flex items-center gap-4">
                        <span className="text-sm text-slate-500">{user?.email}</span>
                        <button
                            onClick={logout}
                            className="text-sm px-3 py-1 text-slate-600 hover:text-red-600 border border-slate-300 rounded hover:bg-red-50 transition"
                        >
                            Вийти
                        </button>
                    </div>
                </div>
            </header>

            {/* Контент */}
            <main className="max-w-7xl mx-auto px-4 py-8">
                <div className="mb-6 flex items-center justify-between">
                    <div>
                        <h2 className="text-xl font-bold text-slate-800 mb-2">Мої ендпоінти</h2>
                        <p className="text-slate-500 text-sm">
                            Hash: <code className="bg-slate-100 px-2 py-1 rounded">{user?.userHash}</code>
                        </p>
                    </div>
                    <button
                        onClick={() => setIsCreateModalOpen(true)}
                        className="px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded hover:bg-blue-700 transition"
                    >
                        + Створити ендпоінт
                    </button>
                </div>

                {/* Таблиця */}
                {endpoints.length === 0 ? (
                    <div className="bg-white rounded-lg p-8 text-center border border-slate-200">
                        <p className="text-slate-400">Ендпоінтів не знайдено</p>
                    </div>
                ) : (
                    <div className="bg-white rounded-lg overflow-hidden shadow-sm border border-slate-200">
                        <table className="w-full">
                            <thead className="bg-slate-50 border-b border-slate-200">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Метод</th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Шлях</th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Статус</th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Full URL</th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase">Дії</th>
                            </tr>
                            </thead>
                            <tbody>
                            {endpoints.map((endpoint) => (
                                <tr key={endpoint.id} className="border-b border-slate-200 hover:bg-slate-50 transition">
                                    <td className="px-6 py-4">
                                        {editingId === endpoint.id ? (
                                            <select
                                                value={editForm.method || ''}
                                                onChange={(e) => setEditForm({ ...editForm, method: e.target.value })}
                                                className="px-2 py-1 border border-slate-300 rounded text-sm"
                                            >
                                                <option value="">Вибрати...</option>
                                                <option value="GET">GET</option>
                                                <option value="POST">POST</option>
                                                <option value="PUT">PUT</option>
                                                <option value="PATCH">PATCH</option>
                                                <option value="DELETE">DELETE</option>
                                            </select>
                                        ) : (
                                            <span className="inline-block px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs font-semibold">
                                                    {endpoint.method}
                                                </span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4">
                                        {editingId === endpoint.id ? (
                                            <input
                                                type="text"
                                                value={editForm.path || ''}
                                                onChange={(e) => setEditForm({ ...editForm, path: e.target.value })}
                                                className="px-2 py-1 border border-slate-300 rounded text-sm w-full"
                                            />
                                        ) : (
                                            <code className="text-sm text-slate-700">{endpoint.path}</code>
                                        )}
                                    </td>
                                    <td className="px-6 py-4">
                                        {editingId === endpoint.id ? (
                                            <input
                                                type="number"
                                                value={editForm.responseStatus || ''}
                                                onChange={(e) =>
                                                    setEditForm({ ...editForm, responseStatus: parseInt(e.target.value) || 200 })
                                                }
                                                className="px-2 py-1 border border-slate-300 rounded text-sm w-20"
                                            />
                                        ) : (
                                            <span
                                                className={`inline-block px-2 py-1 rounded text-xs font-semibold ${
                                                    endpoint.responseStatus >= 200 && endpoint.responseStatus < 300
                                                        ? 'bg-green-100 text-green-700'
                                                        : endpoint.responseStatus >= 400
                                                            ? 'bg-red-100 text-red-700'
                                                            : 'bg-yellow-100 text-yellow-700'
                                                }`}
                                            >
                                                    {endpoint.responseStatus}
                                                </span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-600 font-mono truncate max-w-xs">
                                        {endpoint.fullUrl}
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex gap-2">
                                            {editingId === endpoint.id ? (
                                                <>
                                                    <button
                                                        onClick={handleSave}
                                                        className="px-3 py-1 bg-green-600 text-white text-xs rounded hover:bg-green-700 transition"
                                                    >
                                                        ✓ Зберегти
                                                    </button>
                                                    <button
                                                        onClick={() => setEditingId(null)}
                                                        className="px-3 py-1 bg-slate-400 text-white text-xs rounded hover:bg-slate-500 transition"
                                                    >
                                                        ✕ Скасувати
                                                    </button>
                                                </>
                                            ) : (
                                                <>
                                                    <button
                                                        onClick={() => handleCopyUrl(endpoint.fullUrl)}
                                                        className="px-2 py-1 text-xs bg-slate-200 text-slate-700 rounded hover:bg-slate-300 transition"
                                                        title="Копіювати URL"
                                                    >
                                                        📋 Copy
                                                    </button>
                                                    <button
                                                        onClick={() => handleEditOpen(endpoint)}
                                                        className="px-2 py-1 text-xs bg-blue-200 text-blue-700 rounded hover:bg-blue-300 transition"
                                                        title="Редагувати"
                                                    >
                                                        ✎ Edit
                                                    </button>
                                                    <button
                                                        onClick={() => {
                                                            setSelectedId(endpoint.id);
                                                            setIsModalOpen(true);
                                                        }}
                                                        className="px-2 py-1 text-xs bg-red-200 text-red-700 rounded hover:bg-red-300 transition"
                                                        title="Видалити"
                                                    >
                                                        🗑 Delete
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>

            {/* Модаль створення */}
            <CreateEndpointModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                onSubmit={handleCreate}
            />

            {/* Модаль підтвердження видалення */}
            <Modal
                isOpen={isModalOpen}
                title="Видалити ендпоінт?"
                message="Ця дія не може бути скасована. Ендпоінт буде видален назавжди."
                confirmText="Видалити"
                cancelText="Скасувати"
                isDangerous={true}
                onConfirm={handleDelete}
                onCancel={() => {
                    setIsModalOpen(false);
                    setSelectedId(null);
                }}
            />
        </div>
    );
}
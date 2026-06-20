import { useState } from 'react';
import type { MockEndpoint, CreateEndpointRequest } from '../types';

interface EndpointSettingsFormProps {
    endpoint: MockEndpoint;
    onSave: (data: Partial<CreateEndpointRequest>) => Promise<void>;
}

export default function EndpointSettingsForm({ endpoint, onSave }: EndpointSettingsFormProps) {
    const [form, setForm] = useState<Partial<CreateEndpointRequest>>({
        method: endpoint.method,
        path: endpoint.path,
        responseStatus: endpoint.responseStatus,
        responseBody: endpoint.responseBody || '',
        contentType: endpoint.contentType,
        delayMs: endpoint.delayMs,
    });
    const [isSaving, setIsSaving] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        try {
            await onSave(form);
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-sm border border-slate-200 p-6 space-y-4">
            <h3 className="text-lg font-bold text-slate-800 mb-2">Налаштування</h3>

            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Метод</label>
                    <select
                        value={form.method}
                        onChange={(e) => setForm({ ...form, method: e.target.value })}
                        className="w-full px-3 py-2 border border-slate-300 rounded text-sm"
                    >
                        {['GET', 'POST', 'PUT', 'PATCH', 'DELETE'].map((m) => (
                            <option key={m} value={m}>{m}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Статус</label>
                    <input
                        type="number"
                        value={form.responseStatus ?? ''}
                        onChange={(e) => setForm({ ...form, responseStatus: parseInt(e.target.value) || 200 })}
                        className="w-full px-3 py-2 border border-slate-300 rounded text-sm"
                    />
                </div>
            </div>

            <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Шлях</label>
                <input
                    type="text"
                    value={form.path}
                    onChange={(e) => setForm({ ...form, path: e.target.value })}
                    className="w-full px-3 py-2 border border-slate-300 rounded text-sm font-mono"
                />
            </div>

            <div>
                <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">
                    Response Body (JSON)
                </label>
                {/* TODO(DEV1): замінити на <JsonEditor value={form.responseBody ?? ''} onChange={...} /> */}
                <textarea
                    value={form.responseBody ?? ''}
                    onChange={(e) => setForm({ ...form, responseBody: e.target.value })}
                    rows={6}
                    className="w-full px-3 py-2 border border-slate-300 rounded text-sm font-mono"
                />
            </div>

            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Delay (ms)</label>
                    <input
                        type="number"
                        value={form.delayMs ?? 0}
                        onChange={(e) => setForm({ ...form, delayMs: parseInt(e.target.value) || 0 })}
                        className="w-full px-3 py-2 border border-slate-300 rounded text-sm"
                    />
                </div>
                <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Content-Type</label>
                    <input
                        type="text"
                        value={form.contentType ?? ''}
                        onChange={(e) => setForm({ ...form, contentType: e.target.value })}
                        className="w-full px-3 py-2 border border-slate-300 rounded text-sm font-mono"
                    />
                </div>
            </div>

            <button
                type="submit"
                disabled={isSaving}
                className="px-4 py-2 bg-blue-600 text-white text-sm font-semibold rounded hover:bg-blue-700 transition disabled:opacity-50"
            >
                {isSaving ? 'Збереження…' : '✓ Зберегти'}
            </button>
        </form>
    );
}
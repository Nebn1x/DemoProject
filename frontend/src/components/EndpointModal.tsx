import { useState } from 'react';
import type { FormEvent } from 'react';
import type { CreateEndpointRequest } from '../types';

interface CreateEndpointModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (data: CreateEndpointRequest) => Promise<void>;
}

const initialForm: CreateEndpointRequest = {
    method: 'GET',
    path: '',
    responseStatus: 200,
    responseBody: '',
    contentType: 'application/json',
    delayMs: 0,
};

export default function CreateEndpointModal({ isOpen, onClose, onSubmit }: CreateEndpointModalProps) {
    const [form, setForm] = useState<CreateEndpointRequest>(initialForm);
    const [isSubmitting, setIsSubmitting] = useState(false);

    if (!isOpen) return null;

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            await onSubmit(form);
            setForm(initialForm);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleClose = () => {
        setForm(initialForm);
        onClose();
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-lg p-6">
                <h2 className="text-lg font-bold text-slate-800 mb-4">Новий ендпоінт</h2>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="flex gap-4">
                        <div className="flex-1">
                            <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Метод</label>
                            <select
                                value={form.method}
                                onChange={(e) => setForm({ ...form, method: e.target.value })}
                                className="w-full px-2 py-2 border border-slate-300 rounded text-sm"
                            >
                                <option value="GET">GET</option>
                                <option value="POST">POST</option>
                                <option value="PUT">PUT</option>
                                <option value="PATCH">PATCH</option>
                                <option value="DELETE">DELETE</option>
                            </select>
                        </div>
                        <div className="flex-1">
                            <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Статус</label>
                            <input
                                type="number"
                                value={form.responseStatus}
                                onChange={(e) => setForm({ ...form, responseStatus: parseInt(e.target.value) || 200 })}
                                className="w-full px-2 py-2 border border-slate-300 rounded text-sm"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Шлях</label>
                        <input
                            type="text"
                            placeholder="/api/example"
                            required
                            value={form.path}
                            onChange={(e) => setForm({ ...form, path: e.target.value })}
                            className="w-full px-2 py-2 border border-slate-300 rounded text-sm font-mono"
                        />
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Content-Type</label>
                        <input
                            type="text"
                            value={form.contentType}
                            onChange={(e) => setForm({ ...form, contentType: e.target.value })}
                            className="w-full px-2 py-2 border border-slate-300 rounded text-sm"
                        />
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Response Body</label>
                        <textarea
                            value={form.responseBody}
                            onChange={(e) => setForm({ ...form, responseBody: e.target.value })}
                            rows={4}
                            placeholder='{"message": "ok"}'
                            className="w-full px-2 py-2 border border-slate-300 rounded text-sm font-mono"
                        />
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-600 uppercase mb-1">Затримка (мс)</label>
                        <input
                            type="number"
                            value={form.delayMs}
                            onChange={(e) => setForm({ ...form, delayMs: parseInt(e.target.value) || 0 })}
                            className="w-full px-2 py-2 border border-slate-300 rounded text-sm"
                        />
                    </div>

                    <div className="flex justify-end gap-2 pt-2">
                        <button
                            type="button"
                            onClick={handleClose}
                            className="px-4 py-2 text-sm text-slate-600 border border-slate-300 rounded hover:bg-slate-50 transition"
                        >
                            Скасувати
                        </button>
                        <button
                            type="submit"
                            disabled={isSubmitting || !form.path}
                            className="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 transition disabled:opacity-50"
                        >
                            {isSubmitting ? 'Створення...' : 'Створити'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
import { useState, useEffect, useRef } from 'react';
import type { FormEvent } from 'react';
import type { CreateEndpointRequest, MockEndpoint } from '../types';
import JsonEditor from './JsonEditor';

interface CreateEndpointModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (data: CreateEndpointRequest) => Promise<MockEndpoint | void>;
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
    const [createdUrl, setCreatedUrl] = useState<string | null>(null);
    const [copied, setCopied] = useState(false);
    const pathInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (isOpen && !createdUrl) {
            setTimeout(() => pathInputRef.current?.focus(), 50);
        }
    }, [isOpen, createdUrl]);

    useEffect(() => {
        if (!isOpen) return;
        const handleEsc = (e: KeyboardEvent) => {
            if (e.key === 'Escape' && !isSubmitting) handleClose();
        };
        window.addEventListener('keydown', handleEsc);
        return () => window.removeEventListener('keydown', handleEsc);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isOpen, isSubmitting]);

    if (!isOpen) return null;

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            const result = await onSubmit(form);
            if (result && 'fullUrl' in result) {
                setCreatedUrl(result.fullUrl);
            } else {
                handleClose();
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleClose = () => {
        setForm(initialForm);
        setCreatedUrl(null);
        setCopied(false);
        onClose();
    };

    const handleCopy = () => {
        if (createdUrl) {
            navigator.clipboard.writeText(createdUrl);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        }
    };

    // екран що все ок
    if (createdUrl) {
        return (
            <div
                className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
                onClick={handleClose}
            >
                <div
                    className="bg-white rounded-lg shadow-xl w-full max-w-lg p-6"
                    onClick={(e) => e.stopPropagation()}
                >
                    <div className="text-center mb-4">
                        <div className="text-3xl mb-2">✓</div>
                        <h2 className="text-lg font-bold text-slate-800">Ендпоінт створено!</h2>
                        <p className="text-sm text-slate-500">Ваш mock-URL готовий до використання</p>
                    </div>

                    <div className="bg-slate-100 rounded p-3 mb-4 flex items-center gap-2">
                        <code className="text-sm text-slate-700 flex-1 break-all">{createdUrl}</code>
                        <button
                            onClick={handleCopy}
                            className="px-3 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 transition whitespace-nowrap"
                        >
                            {copied ? '✓ Скопійовано' : '📋 Копіювати'}
                        </button>
                    </div>

                    <button
                        onClick={handleClose}
                        className="w-full px-4 py-2 text-sm bg-slate-800 text-white rounded hover:bg-slate-900 transition"
                    >
                        Готово
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
            onClick={handleClose}
        >
            <div
                className="bg-white rounded-lg shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
            >
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
                            ref={pathInputRef}
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

                    <JsonEditor
                        value={form.responseBody || ''}
                        onChange={(val) => setForm({ ...form, responseBody: val })}
                    />

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

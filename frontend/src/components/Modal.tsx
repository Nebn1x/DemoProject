import React, { useEffect } from 'react';

interface ModalProps {
    isOpen: boolean;
    title: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
    isDangerous?: boolean;
    onConfirm: () => void | Promise<void>;
    onCancel: () => void;
}

export function Modal({
                          isOpen,
                          title,
                          message,
                          confirmText = 'Підтвердити',
                          cancelText = 'Скасувати',
                          isDangerous = false,
                          onConfirm,
                          onCancel,
                      }: ModalProps) {
    const [isLoading, setIsLoading] = React.useState(false);

    // Esc закриває модалку
    useEffect(() => {
        if (!isOpen) return;
        const handleEsc = (e: KeyboardEvent) => {
            if (e.key === 'Escape' && !isLoading) onCancel();
        };
        window.addEventListener('keydown', handleEsc);
        return () => window.removeEventListener('keydown', handleEsc);
    }, [isOpen, isLoading, onCancel]);

    if (!isOpen) return null;

    const handleConfirm = async () => {
        setIsLoading(true);
        try {
            await onConfirm();
        } finally {
            setIsLoading(false);
        }
    };

    const confirmBgColor = isDangerous
        ? 'bg-red-600 hover:bg-red-700'
        : 'bg-blue-600 hover:bg-blue-700';

    return (
        // клік на фон (поза вікном) закриває модалку
        <div
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-40"
            onClick={() => !isLoading && onCancel()}
        >
            <div
                className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4"
                onClick={(e) => e.stopPropagation()} // клік всередині не закриває
            >
                <div className="px-6 py-4 border-b border-slate-200">
                    <h2 className="text-lg font-bold text-slate-800">{title}</h2>
                </div>

                <div className="px-6 py-4">
                    <p className="text-slate-600 text-sm">{message}</p>
                </div>

                <div className="px-6 py-4 bg-slate-50 rounded-b-lg flex gap-3 justify-end">
                    <button
                        onClick={onCancel}
                        disabled={isLoading}
                        className="px-4 py-2 text-slate-700 border border-slate-300 rounded hover:bg-slate-100 transition disabled:opacity-50"
                    >
                        {cancelText}
                    </button>
                    <button
                        onClick={handleConfirm}
                        disabled={isLoading}
                        className={`px-4 py-2 text-white rounded transition disabled:opacity-50 ${confirmBgColor}`}
                    >
                        {isLoading ? 'Обробка...' : confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
}

import Editor from '@monaco-editor/react';
import { useState } from 'react';

interface JsonEditorProps {
    value: string;
    onChange: (value: string) => void;
    height?: string;
}

export default function JsonEditor({ value, onChange, height = '200px' }: JsonEditorProps) {
    const [isValid, setIsValid] = useState(true);

    const handleChange = (val: string | undefined) => {
        const text = val ?? '';
        onChange(text);

        if (text.trim() === '') {
            setIsValid(true);
            return;
        }
        try {
            JSON.parse(text);
            setIsValid(true);
        } catch {
            setIsValid(false);
        }
    };

    const handleFormat = () => {
        try {
            const parsed = JSON.parse(value);
            onChange(JSON.stringify(parsed, null, 2));
            setIsValid(true);
        } catch {
            // якщо невалідний - нічого не робимо
        }
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-semibold text-slate-600 uppercase">Response Body (JSON)</span>
                <div className="flex items-center gap-2">
                    {!isValid && (
                        <span className="text-xs text-red-600">⚠ Невалідний JSON</span>
                    )}
                    {isValid && value.trim() !== '' && (
                        <span className="text-xs text-green-600">✓ Валідний</span>
                    )}
                    <button
                        type="button"
                        onClick={handleFormat}
                        className="text-xs px-2 py-0.5 bg-slate-200 text-slate-700 rounded hover:bg-slate-300 transition"
                    >Форматувати</button>
                </div>
            </div>

            <div className={`border rounded overflow-hidden ${isValid ? 'border-slate-300' : 'border-red-400'}`}>
                <Editor
                    height={height}
                    defaultLanguage="json"
                    value={value}
                    onChange={handleChange}
                    options={{
                        minimap: { enabled: false },
                        fontSize: 13,
                        lineNumbers: 'on',
                        scrollBeyondLastLine: false,
                        automaticLayout: true,
                        tabSize: 2,
                        formatOnPaste: true,
                        wordWrap: 'on',
                    }}
                />
            </div>
        </div>
    );
}

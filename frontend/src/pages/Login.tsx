import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import type { ApiError } from '../types';
import { AxiosError } from 'axios';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async () => {
        setError('');
        setLoading(true);
        try {
            const auth = await authApi.login({ email, password });
            login(auth);
            navigate('/');
        } catch (err) {
            const axiosErr = err as AxiosError<ApiError>;
            setError(axiosErr.response?.data?.message || 'Помилка входу');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
            <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8">
                <h1 className="text-2xl font-bold text-slate-800 mb-1">Вхід</h1>
                <p className="text-slate-500 mb-6 text-sm">Увійдіть у свій акаунт Mock-It</p>

                {error && (
                    <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-lg text-sm">
                        {error}
                    </div>
                )}

                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="you@example.com"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Пароль</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="••••••••"
                        />
                    </div>

                    <button
                        onClick={handleSubmit}
                        disabled={loading}
                        className="w-full bg-blue-600 text-white py-2.5 rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 transition"
                    >
                        {loading ? 'Вхід...' : 'Увійти'}
                    </button>
                </div>

                <p className="text-center text-sm text-slate-500 mt-6">
                    Немає акаунту?{' '}
                    <Link to="/register" className="text-blue-600 hover:underline font-medium">
                        Зареєструватися
                    </Link>
                </p>
            </div>
        </div>
    );
}

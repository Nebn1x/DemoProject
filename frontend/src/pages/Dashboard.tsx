import { useAuth } from '../auth/AuthContext';

/**
 * Заглушка дашборду.
 */
export default function Dashboard() {
    const { user, logout } = useAuth();

    return (
        <div className="min-h-screen bg-slate-50">
            {/* Шапка */}
            <header className="bg-white border-b border-slate-200">
                <div className="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between">
                    <h1 className="text-xl font-bold text-slate-800">Mock-It</h1>
                    <div className="flex items-center gap-4">
                        <span className="text-sm text-slate-500">{user?.email}</span>
                        <button
                            onClick={logout}
                            className="text-sm text-slate-600 hover:text-red-600 transition"
                        >
                            Вийти
                        </button>
                    </div>
                </div>
            </header>

            {/* Контент */}
            <main className="max-w-6xl mx-auto px-4 py-8">
                <div className="bg-white rounded-xl shadow-sm p-6">
                    <h2 className="text-lg font-semibold text-slate-800 mb-2">
                        Вітаємо, {user?.email}!
                    </h2>
                    <p className="text-slate-500 text-sm mb-4">
                        Ваш userHash: <code className="bg-slate-100 px-2 py-0.5 rounded">{user?.userHash}</code>
                    </p>
                    <p className="text-slate-400 text-sm">
                        таблиця mock-ендпоінтів
                    </p>
                </div>
            </main>
        </div>
    );
}

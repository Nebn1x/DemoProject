import { useEndpointLogs } from '../hooks/useEndpointLogs';

interface EndpointLogsProps {
    endpointId: string;
}

function StatusBadge({ status }: { status: number }) {
    const cls =
        status >= 200 && status < 300
            ? 'bg-green-100 text-green-700'
            : status >= 400
                ? 'bg-red-100 text-red-700'
                : 'bg-yellow-100 text-yellow-700';
    return <span className={`inline-block px-2 py-1 rounded text-xs font-semibold ${cls}`}>{status}</span>;
}

function MethodBadge({ method }: { method: string }) {
    const colors: Record<string, string> = {
        GET: 'bg-blue-100 text-blue-700',
        POST: 'bg-green-100 text-green-700',
        PUT: 'bg-amber-100 text-amber-700',
        PATCH: 'bg-amber-100 text-amber-700',
        DELETE: 'bg-red-100 text-red-700',
    };
    const cls = colors[method.toUpperCase()] ?? 'bg-slate-100 text-slate-700';
    return <span className={`inline-block px-2 py-0.5 rounded text-xs font-bold ${cls}`}>{method.toUpperCase()}</span>;
}

function formatTimestamp(ts: string) {
    const date = new Date(ts);
    if (Number.isNaN(date.getTime())) return ts;
    return date.toLocaleString('uk-UA', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
    });
}

export default function EndpointLogs({ endpointId }: EndpointLogsProps) {
    const { logs, isLoading, hasMore, loadMore, isLoadingMore, status, liveCount, newestId } =
        useEndpointLogs(endpointId);

    return (
        <div className="bg-white rounded-lg shadow-sm border border-slate-200">
            {/* Заголовок секції + live-індикатор */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200">
                <h3 className="text-lg font-bold text-slate-800">Останні запити</h3>
                <div className="flex items-center gap-3">
                    {liveCount > 0 && (
                        <span className="text-xs text-slate-400">+{liveCount} нових з моменту відкриття</span>
                    )}
                    <div className="flex items-center gap-1.5 text-xs font-semibold">
                        <span
                            className={`w-2 h-2 rounded-full ${
                                status === 'live'
                                    ? 'bg-green-500 animate-pulse'
                                    : status === 'connecting'
                                        ? 'bg-amber-400 animate-pulse'
                                        : 'bg-slate-300'
                            }`}
                        />
                        <span
                            className={
                                status === 'live'
                                    ? 'text-green-600'
                                    : status === 'connecting'
                                        ? 'text-amber-600'
                                        : 'text-slate-400'
                            }
                        >
                            {status === 'live' ? 'Live' : status === 'connecting' ? "З'єднання…" : 'Offline'}
                        </span>
                    </div>
                </div>
            </div>

            {/* Тіло: завантаження / порожньо / таблиця */}
            {isLoading ? (
                <div className="p-6 space-y-2">
                    {Array.from({ length: 4 }).map((_, i) => (
                        <div key={i} className="h-8 bg-slate-100 rounded animate-pulse" />
                    ))}
                </div>
            ) : logs.length === 0 ? (
                <div className="p-10 text-center text-slate-400 text-sm">
                    Поки що немає запитів до цього ендпоінта.
                    <br />
                    Виклич mock-URL — і лог з'явиться тут одразу.
                </div>
            ) : (
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead className="bg-slate-50 border-b border-slate-200">
                        <tr>
                            <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Метод</th>
                            <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Шлях</th>
                            <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Статус</th>
                            <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Latency</th>
                            <th className="px-6 py-2 text-left text-xs font-semibold text-slate-500 uppercase">Час</th>
                        </tr>
                        </thead>
                        <tbody>
                        {logs.map((logEntry) => (
                            <tr
                                key={logEntry.id}
                                className={`border-b border-slate-100 last:border-0 ${
                                    logEntry.id === newestId ? 'animate-log-row-in' : ''
                                }`}
                            >
                                <td className="px-6 py-2.5">
                                    <MethodBadge method={logEntry.method} />
                                </td>
                                <td className="px-6 py-2.5 text-sm text-slate-700 font-mono">{logEntry.path}</td>
                                <td className="px-6 py-2.5">
                                    <StatusBadge status={logEntry.status} />
                                </td>
                                <td className="px-6 py-2.5 text-sm text-slate-500">{logEntry.latencyMs} ms</td>
                                <td className="px-6 py-2.5 text-sm text-slate-400">
                                    {formatTimestamp(logEntry.timestamp)}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    {hasMore && (
                        <div className="p-4 text-center border-t border-slate-100">
                            <button
                                onClick={loadMore}
                                disabled={isLoadingMore}
                                className="px-4 py-1.5 text-sm bg-slate-100 text-slate-700 rounded hover:bg-slate-200 transition disabled:opacity-50"
                            >
                                {isLoadingMore ? 'Завантаження…' : 'Завантажити ще'}
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
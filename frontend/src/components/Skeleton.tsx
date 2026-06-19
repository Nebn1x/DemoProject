//плейсхолдери замість тексту "Завантаження"
// один рядок таблиці
export function TableRowSkeleton() {
    return (
        <tr className="border-b border-slate-200">
            <td className="px-6 py-4">
                <div className="h-6 w-14 bg-slate-200 rounded animate-pulse" />
            </td>
            <td className="px-6 py-4">
                <div className="h-4 w-32 bg-slate-200 rounded animate-pulse" />
            </td>
            <td className="px-6 py-4">
                <div className="h-6 w-12 bg-slate-200 rounded animate-pulse" />
            </td>
            <td className="px-6 py-4">
                <div className="h-4 w-48 bg-slate-200 rounded animate-pulse" />
            </td>
            <td className="px-6 py-4">
                <div className="flex gap-2">
                    <div className="h-6 w-12 bg-slate-200 rounded animate-pulse" />
                    <div className="h-6 w-12 bg-slate-200 rounded animate-pulse" />
                    <div className="h-6 w-12 bg-slate-200 rounded animate-pulse" />
                </div>
            </td>
        </tr>
    );
}

// скелеттаблиці
export function TableSkeleton({ rows = 4 }: { rows?: number }) {
    return (
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
                {Array.from({ length: rows }).map((_, i) => (
                    <TableRowSkeleton key={i} />
                ))}
                </tbody>
            </table>
        </div>
    );
}

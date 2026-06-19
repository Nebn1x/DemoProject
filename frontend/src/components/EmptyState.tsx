interface EmptyStateProps {
    onCreate: () => void;
}

export default function EmptyState({ onCreate }: EmptyStateProps) {
    return (
        <div className="bg-white rounded-lg p-12 text-center border border-slate-200">
            <div className="text-5xl mb-4">📭</div>
            <h3 className="text-lg font-semibold text-slate-800 mb-1">
                Ще немає жодного ендпоінта
            </h3>
            <p className="text-slate-500 text-sm mb-6 max-w-sm mx-auto">
                Створіть свій перший mock-ендпоінт — задайте метод, шлях і JSON-відповідь,
                і одразу отримаєте готовий URL.
            </p>
            <button
                onClick={onCreate}
                className="px-5 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 transition"
            >
                + Створити перший ендпоінт
            </button>
        </div>
    );
}

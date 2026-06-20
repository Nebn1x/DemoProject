import { useCallback, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { endpointsApi } from '../api/endpoints';
import { tokenStorage } from '../api/client';
import type { RequestLog } from '../types';

type ConnectionStatus = 'connecting' | 'live' | 'offline';

interface UseEndpointLogsResult {
    logs: RequestLog[];
    isLoading: boolean;
    hasMore: boolean;
    loadMore: () => void;
    isLoadingMore: boolean;
    status: ConnectionStatus;
    liveCount: number;
    newestId: string | null;
}

const PAGE_SIZE = 20;

/**
 * Інкапсулює всю логіку логів конкретного ендпоінта:
 *  - початкове + дозавантаження сторінок через REST (GET /api/v1/endpoints/{id}/logs)
 *  - live-підписку на нові логи через STOMP/WebSocket (/topic/logs/{id})
 *
 * Нові логи з WS додаються в начало списку та підраховуються в liveCount
 * (для індикатора "live" і анімації нових рядків на сторінці).
 */
export function useEndpointLogs(endpointId: string | undefined): UseEndpointLogsResult {
    const [logs, setLogs] = useState<RequestLog[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isLoadingMore, setIsLoadingMore] = useState(false);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [status, setStatus] = useState<ConnectionStatus>('connecting');
    const [liveCount, setLiveCount] = useState(0);
    const [newestId, setNewestId] = useState<string | null>(null);

    const clientRef = useRef<Client | null>(null);
    const seenIds = useRef<Set<string>>(new Set());

    // --- початкове завантаження ---
    useEffect(() => {
        if (!endpointId) return;
        let cancelled = false;

        setIsLoading(true);
        setLogs([]);
        setPage(0);
        setHasMore(true);
        seenIds.current = new Set();

        endpointsApi
            .getLogs(endpointId, 0, PAGE_SIZE)
            .then((res) => {
                if (cancelled) return;
                const content = res.data.content;
                content.forEach((l) => seenIds.current.add(l.id));
                setLogs(content);
                setHasMore(res.data.number + 1 < res.data.totalPages);
            })
            .catch((err) => {
                console.error('Помилка завантаження логів', err);
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [endpointId]);

    // --- дозавантаження наступної сторінки (старіші логи) ---
    const loadMore = useCallback(() => {
        if (!endpointId || isLoadingMore || !hasMore) return;
        const nextPage = page + 1;
        setIsLoadingMore(true);

        endpointsApi
            .getLogs(endpointId, nextPage, PAGE_SIZE)
            .then((res) => {
                const fresh = res.data.content.filter((l) => !seenIds.current.has(l.id));
                fresh.forEach((l) => seenIds.current.add(l.id));
                setLogs((prev) => [...prev, ...fresh]);
                setPage(nextPage);
                setHasMore(res.data.number + 1 < res.data.totalPages);
            })
            .catch((err) => {
                console.error('Помилка дозавантаження логів', err);
            })
            .finally(() => setIsLoadingMore(false));
    }, [endpointId, page, hasMore, isLoadingMore]);

    // --- live WebSocket-підписка ---
    useEffect(() => {
        if (!endpointId) return;

        const token = tokenStorage.get();
        setStatus('connecting');

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const brokerURL = `${protocol}//${window.location.host}/ws/logs`;

        const client = new Client({
            brokerURL,
            connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
            reconnectDelay: 3000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            onConnect: () => {
                setStatus('live');
                client.subscribe(`/topic/logs/${endpointId}`, (message) => {
                    try {
                        const incoming: RequestLog = JSON.parse(message.body);
                        if (seenIds.current.has(incoming.id)) return;
                        seenIds.current.add(incoming.id);

                        setLogs((prev) => [incoming, ...prev]);
                        setLiveCount((c) => c + 1);
                        setNewestId(incoming.id);
                    } catch (err) {
                        console.error('Не вдалось розпарсити WS-повідомлення логу', err);
                    }
                });
            },
            onDisconnect: () => setStatus('offline'),
            onWebSocketClose: () => setStatus('offline'),
            onStompError: (frame) => {
                console.error('STOMP помилка', frame.headers['message'], frame.body);
                setStatus('offline');
            },
        });

        clientRef.current = client;
        client.activate();

        return () => {
            client.deactivate();
            clientRef.current = null;
        };
    }, [endpointId]);

    return { logs, isLoading, hasMore, loadMore, isLoadingMore, status, liveCount, newestId };
}
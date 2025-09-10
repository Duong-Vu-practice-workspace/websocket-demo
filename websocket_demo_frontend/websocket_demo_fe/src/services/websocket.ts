import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let client: Client | null = null;
let listeners: Array<(payload: any) => void> = [];

export function connectWS(accessToken: string, onMessage: (payload: any) => void) {
    if (client && client.active) return client;

    client = new Client({
        webSocketFactory: () => new SockJS(`${import.meta.env.VITE_BACKEND_URL}/ws`),
        connectHeaders: {
            Authorization: accessToken ? `Bearer ${accessToken}` : ''
        },
        debug: (str) => { /* console.log(str) */ },
        onConnect: () => {
            console.log('WS: Connected successfully');
            console.log('WS: Subscribing to /topic/backup-status');
            // subscribe to broadcast topic
            client!.subscribe('/topic/backup-status', (msg) => {
                const body = msg.body ? JSON.parse(msg.body) : null;
                console.log('WS: Received on /topic/backup-status:', msg.body);
                // call the connectWS caller callback
                try { onMessage(body); } catch (e) { /* ignore */ }
                // call any registered listeners
                listeners.forEach(cb => {
                    try { cb(body); } catch (e) { /* ignore */ }
                });
            });
            console.log('WS: Subscribing to /user/queue/backup-status');
            // subscribe to user-specific queue (backend must send to /user/{username}/queue/backup-status)
            client!.subscribe('/user/queue/backup-status', (msg) => {
                const body = msg.body ? JSON.parse(msg.body) : null;
                console.log('WS: Received on /user/queue/backup-status:', msg.body);
                try { onMessage(body); } catch (e) { /* ignore */ }
                listeners.forEach(cb => {
                    try { cb(body); } catch (e) { /* ignore */ }
                });
            });
        },
        onStompError: (frame) => {
            console.error('STOMP error', frame);
        },
        reconnectDelay: 5000
    });
    client.activate();
    return client;
}

export function disconnectWS() {
    client?.deactivate();
    client = null;
    listeners = [];
}

// Allow components to subscribe to incoming messages (returns unsubscribe function)
export function addWSListener(cb: (payload: any) => void) {
    listeners.push(cb);
    return () => {
        const idx = listeners.indexOf(cb);
        if (idx >= 0) listeners.splice(idx, 1);
    };
}